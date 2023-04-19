/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.GZIPOutputStream;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.api.UrlUtils;
import org.mule.module.apikit.api.console.ConsoleResources;
import org.mule.module.apikit.api.console.Resource;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.helpers.EventHelper;
import org.mule.module.apikit.helpers.EventWrapper;
import org.mule.runtime.api.component.AbstractComponent;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.util.StringMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Optional;

import static java.lang.Boolean.valueOf;
import static org.mule.apikit.ApiType.AMF;
import static org.mule.module.apikit.ApikitErrorTypes.errorRepositoryFrom;
import static org.mule.module.apikit.ApikitErrorTypes.throwErrorType;
import static org.mule.module.apikit.api.FlowUtils.getSourceLocation;
import static org.mule.module.apikit.api.UrlUtils.getBaseUriReplacement;
import static org.mule.module.apikit.api.UrlUtils.replaceHostInURL;
import static org.mule.module.apikit.helpers.AttributesHelper.getAcceptedResponseMediaTypes;
import static org.mule.module.apikit.helpers.ConfigURLMapping.INSTANCE;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;

public class Console extends AbstractComponent implements Processor, Initialisable, Disposable {

  private final ApikitRegistry registry;
  private final ConfigurationComponentLocator locator;

  private Configuration configuration;
  private String name;
  protected static final Logger logger = LoggerFactory.getLogger(Console.class);

  private static final String CONSOLE_URL_FILE = "consoleurl";

  private static final String CONSOLE_DISABLED = "apikit.console.disabled";

  private boolean consoleDisabled;

  private boolean streamAMFModel;

  private static final String STREAM_AMF_MODEL = "apikit.console.stream.amf.model";

  @Inject
  private MuleContext muleContext;

  @Inject
  private SchedulerService schedulerService;

  private Scheduler scheduler;

  @Inject
  public Console(ApikitRegistry registry, ConfigurationComponentLocator locator) {
    this.registry = registry;
    this.locator = locator;
  }

  @Override
  public void initialise() {
    consoleDisabled = valueOf(System.getProperty(CONSOLE_DISABLED, "false"));
    streamAMFModel = valueOf(System.getProperty(STREAM_AMF_MODEL, "true"));
    final String name = getLocation().getRootContainerName();
    final Optional<URI> url = getSourceLocation(locator, name);

    if (url.isPresent()) {
      URI uri = url.get();
      String consoleUrl = uri.toString().replace("*", "");
      String consoleUrlFixed = getBaseUriReplacement(consoleUrl);
      logger.info(StringMessageUtils.getBoilerPlate("APIKit Console URL: " + consoleUrlFixed));
      publishConsoleUrls(consoleUrlFixed);
    } else {
      logger.error("There was an error retrieving console source.");
    }
    this.scheduler = getScheduler();
  }

  @Override
  public CoreEvent process(CoreEvent event) {

    if (consoleDisabled) {
      throw throwErrorType(new NotFoundException("Not Found"), errorRepositoryFrom(muleContext));
    }
    final Configuration config = getConfiguration();

    EventWrapper eventWrapper = new EventWrapper(event, config.getOutboundHeadersMapName(), config.getHttpStatusVarName());

    HttpRequestAttributes attributes = EventHelper.getHttpRequestAttributes(event);
    String listenerPath = attributes.getListenerPath();
    String requestPath = attributes.getRequestPath();
    String acceptHeader = getAcceptedResponseMediaTypes(attributes.getHeaders());
    String queryString = attributes.getQueryString();
    String method = attributes.getMethod();
    String host = attributes.getHeaders().get("host");

    ConsoleResources consoleResources = new ConsoleResources(config, listenerPath,
                                                             requestPath, queryString, method, acceptHeader,
                                                             errorRepositoryFrom(muleContext),
                                                             replaceHostInURL(INSTANCE.getUrl(config.getName()), host));

    // Listener path MUST end with /*
    consoleResources.isValidPath(attributes.getListenerPath());

    String consoleBasePath = UrlUtils.getBasePath(listenerPath, requestPath);
    String resourceRelativePath = UrlUtils.getRelativePath(listenerPath, requestPath);

    // If the request was made to, for example, /console, we must redirect the client to /console/
    if (!consoleBasePath.endsWith("/")) {
      eventWrapper.doClientRedirect();
      return eventWrapper.build();
    }

    Resource resource =
        streamAMFModel && AMF.equals(config.getType()) && "amf".equals(queryString)
            ? streamAMFModel(replaceHostInURL(INSTANCE.getUrl(config.getName()), host))
            : consoleResources.getConsoleResource(resourceRelativePath);

    eventWrapper.setPayload(resource.getContent() == null ? "" : resource.getContent(), resource.getMediaType());
    eventWrapper.addOutboundProperties(resource.getHeaders());
    return eventWrapper.build();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private void publishConsoleUrls(final String consoleUrl) {
    FileWriter writer = null;

    try {
      final String parentDirectory = muleContext.getConfiguration().getWorkingDirectory();
      File urlFile = new File(parentDirectory, CONSOLE_URL_FILE);
      if (!urlFile.exists()) {
        urlFile.createNewFile();
      }
      writer = new FileWriter(urlFile, true);
      writer.write(consoleUrl + "\n");
      writer.flush();
    } catch (Exception e) {
      logger.error("cannot publish console url for studio", e);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * Triggers AMF model writing in another Thread, in a PipedOutputStream
   * 
   * @param url
   * @return Resource with a PipedInputStream for reading the model as a Stream
   */
  private Resource streamAMFModel(String url) {
    try {
      PipedOutputStream pipedOutputStream = new PipedOutputStream();
      InputStream responsePayload = new PipedInputStream(pipedOutputStream);
      scheduler.execute(() -> {
        try {
          configuration.getRamlHandler().writeAMFModel(url, new GZIPOutputStream(pipedOutputStream));
        } catch (Exception e) {
          logger.error("Error trying to stream AMF Model");
        }
      });

      return new Resource() {

        @Override
        public MediaType getMediaType() {
          return APPLICATION_JSON;
        }

        @Override
        public Object getContent() {
          return responsePayload;
        }

        @Override
        public MultiMap<String, String> getHeaders() {
          MultiMap<String, String> headers = new MultiMap<>();
          headers.put("Content-Encoding", "gzip");
          return headers;
        }
      };
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void dispose() {
    this.scheduler.shutdownNow();
  }

  private Scheduler getScheduler() {
    SchedulerConfig config = SchedulerConfig.config()
        .withName("CONSOLE-SCHEDULER");

    return schedulerService.ioScheduler(config);
  }
}
