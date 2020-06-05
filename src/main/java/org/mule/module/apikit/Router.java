/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.google.common.cache.LoadingCache;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.Resource;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.api.exception.BadRequestException;
import org.mule.module.apikit.api.exception.InvalidUriParameterException;
import org.mule.module.apikit.api.exception.MuleRestException;
import org.mule.module.apikit.api.spi.AbstractRouter;
import org.mule.module.apikit.api.spi.RouterService;
import org.mule.module.apikit.api.uri.ResolvedVariables;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.api.validation.ValidRequest;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.routing.DefaultFlowRoutingStrategy;
import org.mule.module.apikit.routing.FlowRoutingStrategy;
import org.mule.module.apikit.routing.PrivilegedFlowRoutingStrategy;
import org.mule.module.apikit.uri.URICoder;
import org.mule.module.apikit.utils.MuleVersionUtils;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Flow;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.streaming.StreamingManager;
import org.mule.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

import static org.mule.module.apikit.ApikitErrorTypes.errorRepositoryFrom;
import static org.mule.module.apikit.ApikitErrorTypes.throwErrorType;
import static org.mule.module.apikit.api.FlowUtils.getSourceLocation;
import static org.mule.module.apikit.api.validation.RequestValidator.validate;
import static org.mule.module.apikit.helpers.AttributesHelper.getContentType;
import static org.mule.runtime.core.api.util.StringMessageUtils.getBoilerPlate;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.flatMap;
import static org.mule.runtime.core.privileged.processor.MessageProcessors.processToApply;
import static reactor.core.publisher.Mono.error;


public class Router extends AbstractComponent implements Processor, Initialisable, AbstractRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  @Inject
  private MuleContext muleContext;

  private final ApikitRegistry registry;
  private final ConfigurationComponentLocator locator;

  private FlowRoutingStrategy routingStrategy;
  private Configuration configuration;
  private String name;

  @Inject
  private StreamingManager streamingManager;

  private CursorStreamProviderFactory streamProviderFactory;

  @Inject
  public Router(ApikitRegistry registry, ConfigurationComponentLocator locator) {
    this.registry = registry;
    this.locator = locator;
  }

  @Override
  public void initialise() {
    String name = getLocation().getRootContainerName();
    Optional<URI> url = getSourceLocation(locator, name);
    this.routingStrategy = getRoutingStrategy();
    if (!url.isPresent()) {
      LOGGER
          .error("There was an error retrieving Api Source. Console will work only if the keepApiBaseUri property is set to true.");
    } else {
      String configName = configuration.getName();
      registry.setApiSource(configName, url.get().toString().replace("*", ""));
      LOGGER.info(getBoilerPlate("APIKit Router '" + configName + "' started using Parser: " + configuration.getType()));
    }
    this.streamProviderFactory = streamingManager.forBytes().getDefaultCursorProviderFactory();
  }

  private FlowRoutingStrategy getRoutingStrategy() {
    // privileged API should only be used in MULE 4.1.x versions, since 4.2.0 we start using the ExecutableComponent public API
    return MuleVersionUtils.isAtLeast("4.2.0") ? new DefaultFlowRoutingStrategy()
        : new PrivilegedFlowRoutingStrategy(getLocation());
  }

  @Override
  public CoreEvent process(final CoreEvent event) throws MuleException {
    return processToApply(event, this);
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return flatMap(publisher, this::processWithExtension, this);
  }

  @Override
  public ApiSpecification getRaml() {
    return getConfiguration().getRamlHandler().getApi();
  }

  private Publisher<CoreEvent> processWithExtension(CoreEvent event) {
    try {
      Optional<RouterService> extension = configuration.getExtension();
      if (extension.isPresent()) {
        return extension.get().process(event, this);
      } else {
        return processEvent(event);
      }
    } catch (MuleRestException e) {
      return error(throwErrorType(e, errorRepositoryFrom(muleContext)));
    } catch (MuleException e) {
      return error(e);
    }
  }

  @Override
  public Publisher<CoreEvent> processEvent(CoreEvent event) throws MuleRestException {
    Configuration config = registry.getConfiguration(getConfiguration().getName());
    HttpRequestAttributes attributes = ((HttpRequestAttributes) event.getMessage().getAttributes().getValue());
    return doRoute(event, config, attributes);
  }

  private Publisher<CoreEvent> doRoute(CoreEvent mainEvent, Configuration config, HttpRequestAttributes attributes)
      throws MuleRestException {

    String path = getRequestPath(attributes);
    // Get uriPattern, uriResolver, and the resolvedVariables
    URIPattern uriPattern = findInCache(path, config.getUriPatternCache());
    URIResolver uriResolver = findInCache(path, config.getUriResolverCache());
    ResolvedVariables resolvedVariables = uriResolver.resolve(uriPattern);
    Resource resource = config.getFlowFinder().getResource(uriPattern);

    TypedValue<Object> payload = mainEvent.getMessage().getPayload();

    ValidRequest request = validate(config, resource, attributes, resolvedVariables,
                                    makeInputBodyRepeatable(config.isDisableValidations(), mainEvent, payload),
                                    errorRepositoryFrom(muleContext));

    Flow flow = config.getFlowFinder().getFlow(resource,
                                               attributes.getMethod().toLowerCase(),
                                               getContentType(attributes.getHeaders()));

    CoreEvent subFlowEvent = buildSubFlowEvent(config.isDisableValidations(),
                                               mainEvent, request,
                                               config.getOutboundHeadersMapName());

    return Mono.from(routingStrategy.route(flow, mainEvent, subFlowEvent))
        .map(result -> {
          if (result.getVariables().get(config.getHttpStatusVarName()) == null) {
            // If status code is missing, a default one is added
            RamlHandler handler = config.getRamlHandler();
            String successStatusCode = handler.getSuccessStatusCode(resource.getAction(attributes.getMethod().toLowerCase()));
            return CoreEvent.builder(result).addVariable(config.getHttpStatusVarName(), successStatusCode).build();
          }
          return result;
        });
  }

  private String getRequestPath(HttpRequestAttributes attributes) throws BadRequestException {
    try {
      String rawRequestPath = URICoder.encodeRequestPath(attributes.getRawRequestPath());
      String path = UrlUtils.getRelativePath(attributes.getListenerPath(), rawRequestPath);
      return path.isEmpty() ? "/" : path;
    } catch (ApikitRuntimeException e) {
      throw new InvalidUriParameterException(e.getMessage());
    }

  }

  private <T> T findInCache(String key, LoadingCache<String, T> cache) {
    try {
      return cache.get(key);
    } catch (Exception e) {
      throw throwErrorType(new NotFoundException(key), errorRepositoryFrom(muleContext));
    }
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration config) {
    this.configuration = config;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private CoreEvent buildSubFlowEvent(boolean disableValidations, CoreEvent parent,
                                      ValidRequest request, String outboundHeadersMapName) {

    CoreEvent.Builder eventBuilder = CoreEvent.builder(parent);
    eventBuilder.addVariable(outboundHeadersMapName, new HashMap<>());

    Message.Builder messageBuilder = Message.builder(parent.getMessage());
    messageBuilder.value(makeRepeatable(disableValidations, request.getBody().getPayload(), parent));
    messageBuilder.attributesValue(request.getAttributes());
    return eventBuilder.message(messageBuilder.build()).build();
  }

  private TypedValue<Object> makeInputBodyRepeatable(boolean disableValidations, CoreEvent event,
                                                     TypedValue<Object> inputBody) {
    return new TypedValue<>(makeRepeatable(disableValidations, inputBody.getValue(), event),
                            inputBody.getDataType(),
                            inputBody.getByteLength());
  }


  private Object makeRepeatable(boolean disableValidations, Object body, CoreEvent event) {
    try {
      if (disableValidations) {
        return body;
      }
      // available > 0, avoid make repeatable an empty InputStream (request without body)
      return body instanceof InputStream && ((InputStream) body).available() > 0
          ? streamProviderFactory.of(event.getContext(), (InputStream) body)
          : body;
    } catch (IOException e) {
      return body;
    }
  }
}
