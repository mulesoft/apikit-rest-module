/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.github.fge.jsonschema.main.JsonSchema;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang.StringUtils;
import org.mule.apikit.ApiType;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.config.ConsoleConfig;
import org.mule.module.apikit.api.config.ValidationConfig;
import org.mule.module.apikit.api.exception.ApikitRuntimeException;
import org.mule.module.apikit.api.spi.RouterService;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.api.validation.ApiKitJsonSchema;
import org.mule.module.apikit.validation.body.schema.v1.cache.JsonSchemaCacheLoader;
import org.mule.module.apikit.validation.body.schema.v1.cache.XmlSchemaCacheLoader;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;

import static java.lang.Runtime.getRuntime;
import static org.mule.module.apikit.ApikitErrorTypes.errorRepositoryFrom;

public class Configuration implements Disposable, Initialisable, ValidationConfig, ConsoleConfig {

  private static final String DEFAULT_OUTBOUND_HEADERS_MAP_NAME = "outboundHeaders";
  private static final String DEFAULT_HTTP_STATUS_VAR_NAME = "httpStatus";
  private static final int URI_CACHE_SIZE = 1000;
  public static final String MULE_EXTERNAL_ENTITIES_PROPERTY = "mule.xml.expandExternalEntities";
  public static final String MULE_EXPAND_ENTITIES_PROPERTY = "mule.xml.expandInternalEntities";


  protected static final Logger logger = LoggerFactory.getLogger(Configuration.class);
  protected LoadingCache<String, URIResolver> uriResolverCache;
  protected LoadingCache<String, URIPattern> uriPatternCache;

  private boolean disableValidations;
  private ApikitParserMode parserMode;
  private boolean queryParamsStrictValidation;
  private boolean headersStrictValidation;
  private String name;
  private String raml;
  private String api;
  private boolean keepApiBaseUri;
  private boolean keepRamlBaseUri;
  private String outboundHeadersMapName;
  private String httpStatusVarName;
  private FlowMappings flowMappings = new FlowMappings();

  private LoadingCache<String, JsonSchema> jsonSchemaCache;
  private LoadingCache<String, Schema> xmlSchemaCache;

  private RamlHandler ramlHandler;
  private FlowFinder flowFinder;
  private Optional<RouterService> routerService;

  // DO NOT USE: does nothing just keeping it for Backwards compatibility, the routerService optional does the jobs for this.
  private boolean extensionEnabled;

  @Inject // TODO delete this after getting resources from resource folder and the flows
  private MuleContext muleContext;

  @Inject
  private ApikitRegistry registry;

  @Inject
  private ExpressionManager expressionManager;

  @Inject
  private ConfigurationComponentLocator locator;

  @Inject
  private SchedulerService schedulerService;

  private Scheduler scheduler;

  @Override
  public void initialise() throws InitialisationException {
    xmlEntitiesConfiguration();
    this.routerService = findExtension();
    this.scheduler = getScheduler();

    try {
      ramlHandler = new RamlHandler(this.scheduler, getApi(), isKeepApiBaseUri(),
                                    errorRepositoryFrom(muleContext), parserMode.get());
      this.routerService.ifPresent(rs -> {
        try {
          rs.initialise(ramlHandler.getApi().getUri());
        } catch (MuleException e) {
          throw new ApikitRuntimeException("Couldn't load enabled extension", e);
        }
      });
    } catch (Exception e) {
      throw new InitialisationException(e.fillInStackTrace(), this);
    }
    flowFinder = new FlowFinder(ramlHandler, getName(), locator, flowMappings.getFlowMappings(),
                                errorRepositoryFrom(muleContext));
    buildResourcePatternCaches();
    registry.registerConfiguration(this);
  }

  @Deprecated // TODO USE NEW API
  public String getApiServer() {
    return "http://localhost:8081";
  }

  // config properties
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRaml() {
    return raml;
  }

  public void setRaml(String raml) {
    this.raml = raml;
  }

  public String getApi() {
    return StringUtils.isEmpty(api) ? raml : api;
  }

  public void setApi(String api) {
    this.api = api;
  }

  public boolean isDisableValidations() {
    return disableValidations;
  }

  public void setDisableValidations(boolean disableValidations) {
    this.disableValidations = disableValidations;
  }

  public void setParser(ApikitParserMode parserType) {
    this.parserMode = parserType;
  }

  @Override
  public boolean isQueryParamsStrictValidation() {
    return queryParamsStrictValidation;
  }

  public void setQueryParamsStrictValidation(boolean queryParamsStrictValidation) {
    this.queryParamsStrictValidation = queryParamsStrictValidation;
  }

  @Override
  public boolean isHeadersStrictValidation() {
    return headersStrictValidation;
  }

  public void setHeadersStrictValidation(boolean headersStrictValidation) {
    this.headersStrictValidation = headersStrictValidation;
  }

  @Deprecated
  public boolean isKeepRamlBaseUri() {
    return keepRamlBaseUri;
  }

  public boolean isKeepApiBaseUri() {
    return keepApiBaseUri || isKeepRamlBaseUri();
  }

  @Deprecated
  public void setKeepRamlBaseUri(boolean keepRamlBaseUri) {
    this.keepRamlBaseUri = keepRamlBaseUri;
  }

  public void setKeepApiBaseUri(boolean keepApiBaseUri) {
    this.keepApiBaseUri = keepApiBaseUri;
  }

  public FlowMappings getFlowMappings() {
    return flowMappings;
  }

  public void setFlowMappings(FlowMappings flowMappings) {
    this.flowMappings = flowMappings;
  }

  public String getOutboundHeadersMapName() {
    if (outboundHeadersMapName == null) {
      return DEFAULT_OUTBOUND_HEADERS_MAP_NAME;
    }
    return outboundHeadersMapName;
  }

  public void setOutboundHeadersMapName(String outboundHeadersMapName) {
    this.outboundHeadersMapName = outboundHeadersMapName;
  }

  public String getHttpStatusVarName() {
    if (httpStatusVarName == null) {
      return DEFAULT_HTTP_STATUS_VAR_NAME;
    }
    return httpStatusVarName;
  }

  public void setHttpStatusVarName(String httpStatusVarName) {
    this.httpStatusVarName = httpStatusVarName;
  }

  private void buildResourcePatternCaches() {
    logger.info("Building resource URI cache...");
    uriResolverCache = CacheBuilder.newBuilder()
        .maximumSize(URI_CACHE_SIZE)
        .build(
               new CacheLoader<String, URIResolver>() {

                 @Override
                 public URIResolver load(String path) throws IOException {
                   return new URIResolver(path);
                 }
               });

    uriPatternCache = CacheBuilder.newBuilder()
        .maximumSize(URI_CACHE_SIZE)
        .build(
               new CacheLoader<String, URIPattern>() {

                 @Override
                 public URIPattern load(String path) throws Exception {
                   URIResolver resolver = uriResolverCache.get(path);
                   URIPattern match = flowFinder.findBestMatch(resolver);

                   if (match == null) {
                     logger.warn("No matching patterns for URI " + path);
                     throw new IllegalStateException("No matching patterns for URI " + path);
                   }
                   return match;
                 }
               });
  }

  public FlowFinder getFlowFinder() {
    return flowFinder;
  }


  // uri caches
  public LoadingCache<String, URIPattern> getUriPatternCache() {
    return uriPatternCache;
  }

  public LoadingCache<String, URIResolver> getUriResolverCache() {
    return uriResolverCache;
  }

  // schema caches
  public LoadingCache<String, JsonSchema> getJsonSchemaCache() {
    if (jsonSchemaCache == null) {
      jsonSchemaCache = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .build(new JsonSchemaCacheLoader(ramlHandler.getApi()));
    }
    return jsonSchemaCache;
  }

  public LoadingCache<String, Schema> getXmlSchemaCache() {
    if (xmlSchemaCache == null) {
      LoadingCache<String, Schema> transformerCache = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .build(new XmlSchemaCacheLoader(ramlHandler.getApi()));

      xmlSchemaCache = transformerCache;
    }
    return xmlSchemaCache;
  }

  public void setRamlHandler(RamlHandler ramlHandler) {
    this.ramlHandler = ramlHandler; // TODO REPLACE WITH REFLECTION
  }

  @Override
  public RamlHandler getRamlHandler() {
    return this.ramlHandler;
  }

  @Override
  public ApiType getType() {
    return ramlHandler.getApi().getType();
  }

  @Override
  public boolean isParserV2() {
    return getRamlHandler().isParserV2();
  }

  @Override
  public ApiKitJsonSchema getJsonSchema(String schemaPath) throws ExecutionException {

    try {
      return new ApiKitJsonSchema(getJsonSchemaCache().get(schemaPath));

    } catch (CacheLoader.InvalidCacheLoadException e) {

      // Schema is not defined in the RAML
      return null;
    }
  }

  @Override
  public Schema getXmlSchema(String schemaPath) throws ExecutionException {
    return getXmlSchemaCache().get(schemaPath);
  }

  @Override
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  private Optional<RouterService> findExtension() {
    ClassLoader executionClassLoader = muleContext.getExecutionClassLoader();
    ServiceLoader<RouterService> routerServices = ServiceLoader.load(RouterService.class, executionClassLoader);
    Iterator<RouterService> iterator = routerServices.iterator();
    return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
  }

  public Optional<RouterService> getExtension() {
    return this.routerService;
  }

  private void xmlEntitiesConfiguration() {
    String externalEntities = System.getProperty(MULE_EXTERNAL_ENTITIES_PROPERTY, "false");
    System.setProperty("raml.xml.expandExternalEntities", externalEntities);
    System.setProperty("amf.plugins.xml.expandExternalEntities", externalEntities);

    String internalEntities = System.getProperty(MULE_EXPAND_ENTITIES_PROPERTY, "false");
    System.setProperty("raml.xml.expandInternalEntities", internalEntities);
    System.setProperty("amf.plugins.xml.expandInternalEntities", internalEntities);
  }

  private Scheduler getScheduler() {
    SchedulerConfig config = SchedulerConfig.config()
        .withMaxConcurrentTasks(getRuntime().availableProcessors())
        .withName("AMF-SCHEDULER");

    return schedulerService.customScheduler(config, Integer.MAX_VALUE);
  }

  @Override
  public void dispose() {
    scheduler.shutdownNow();
  }
}
