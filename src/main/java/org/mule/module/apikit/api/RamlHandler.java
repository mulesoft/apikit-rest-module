/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api;

import org.apache.commons.io.IOUtils;
import org.mule.amf.impl.model.AMFImpl;
import org.mule.apikit.ApiType;
import org.mule.apikit.loader.ApiSyncResourceLoader;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.ApiSpecification;
import org.mule.apikit.model.ApiVendor;
import org.mule.apikit.model.api.ApiReference;
import org.mule.module.apikit.StreamUtils;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.parser.service.result.ParsingIssue;
import org.mule.parser.service.result.UnsupportedParsingIssue;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.api.exception.TypedException;
import org.raml.model.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.mule.apikit.ApiType.AMF;
import static org.mule.apikit.ApiType.RAML;
import static org.mule.apikit.common.ApiSyncUtils.isSyncProtocol;
import static org.mule.apikit.model.ApiVendor.RAML_08;
import static org.mule.apikit.model.ApiVendor.RAML_10;
import static org.mule.module.apikit.ApikitErrorTypes.throwErrorType;
import static org.mule.parser.service.ParserMode.AUTO;
import static org.mule.runtime.core.api.util.StringMessageUtils.getBoilerPlate;

public class RamlHandler {

  private Pattern CONSOLE_RESOURCE_PATTERN = Pattern.compile(".*console-resources.*(html|json|js)");

  public static final String APPLICATION_RAML = "application/raml+yaml";

  private static final Logger LOGGER = LoggerFactory.getLogger(RamlHandler.class);
  private static final String RAML_QUERY_STRING = "raml";
  private String apiResourcesRelativePath = "";

  private boolean keepApiBaseUri;
  private String apiServer;
  private ApiSpecification api;
  private ErrorTypeRepository errorTypeRepository;
  private List<String> acceptedClasspathResources;

  // ramlLocation should be the root raml location, relative of the resources folder
  public RamlHandler(String ramlLocation, boolean keepApiBaseUri, ErrorTypeRepository errorTypeRepository) throws IOException {
    this(null, ramlLocation, keepApiBaseUri, errorTypeRepository, null);
  }

  public RamlHandler(String ramlLocation,
                     boolean keepApiBaseUri,
                     ErrorTypeRepository errorTypeRepository,
                     ParserMode parserMode)
      throws IOException {
    this(null, ramlLocation, keepApiBaseUri, errorTypeRepository, parserMode);
  }

  public RamlHandler(ScheduledExecutorService executor,
                     String ramlLocation,
                     boolean keepApiBaseUri,
                     ErrorTypeRepository errorTypeRepository,
                     ParserMode parserMode)
      throws IOException {

    this(executor, ramlLocation, keepApiBaseUri, errorTypeRepository, parserMode, false);
  }

  public RamlHandler(ScheduledExecutorService executor,
                     String ramlLocation,
                     boolean keepApiBaseUri,
                     ErrorTypeRepository errorTypeRepository,
                     ParserMode parserMode, boolean filterUnsupportedLogging)
      throws IOException {
    ParserService parserService = new ParserService(executor);
    this.keepApiBaseUri = keepApiBaseUri;
    String rootRamlLocation = findRootRaml(ramlLocation);

    if (rootRamlLocation == null) {
      throw new IOException("Raml not found at: " + ramlLocation);
    }
    ApiReference apiReference = ApiReference.create(rootRamlLocation);
    ParseResult result = parserService.parse(apiReference, parserMode == null ? AUTO : parserMode);
    if (result.success()) {
      logWarnings(result.getWarnings(), filterUnsupportedLogging);
      this.api = result.get();
      int idx = rootRamlLocation.lastIndexOf("/");
      if (idx > 0) {
        this.apiResourcesRelativePath = rootRamlLocation.substring(0, idx + 1);
        this.apiResourcesRelativePath = uriPathToResourcePath(apiResourcesRelativePath);
      } else if (isSyncProtocol(rootRamlLocation)) {
        this.apiResourcesRelativePath = rootRamlLocation;
      }
      this.acceptedClasspathResources = getAcceptedClasspathResources(api, apiResourcesRelativePath);
      this.errorTypeRepository = errorTypeRepository;
    } else {
      String errors = result.getErrors().stream().map(e -> "  - " + e.cause()).collect(joining(" \n"));
      throw new RuntimeException("Errors while parsing RAML file in [" + parserMode + "] mode: \n" + errors);
    }
  }

  private void logWarnings(List<ParsingIssue> parsingIssues, boolean filterUnsupportedLogging) {
    if (isNotEmpty(parsingIssues)) {
      LOGGER
          .warn(getBoilerPlate(getFilteredParsingIssueStream(parsingIssues, filterUnsupportedLogging).map(e -> "  - " + e.cause())
              .collect(joining(" \n"))));
    }
  }

  private Stream<ParsingIssue> getFilteredParsingIssueStream(List<ParsingIssue> parsingIssues, boolean filterUnsupportedLogging) {
    return parsingIssues.stream()
        .filter(issue -> !(issue instanceof UnsupportedParsingIssue)
            || (issue instanceof UnsupportedParsingIssue && !filterUnsupportedLogging));
  }

  private List<String> getAcceptedClasspathResources(ApiSpecification api, String apiResourcesRelativePath) {
    return api.getAllReferences().stream()
        .map(ref -> {
          try {
            return Paths.get(new URI(ref)).toString();
          } catch (Exception e) {
            return ref;
          }
        })
        .map(ref -> {
          int index = ref.indexOf(apiResourcesRelativePath);
          return index > 0 ? ref.substring(index) : ref;
        })
        .collect(toList());
  }

  /**
   * @deprecated use getApiVendor() instead.
   */
  @Deprecated
  public boolean isParserV2() {
    ApiType parser = api.getType();
    return parser == AMF || (parser == RAML && ApiVendor.RAML_10 == getApiVendor());
  }

  public ApiVendor getApiVendor() {
    return api.getApiVendor();
  }

  public ApiSpecification getApi() {
    return api;
  }

  public void setApi(ApiSpecification api) {
    this.api = api;
  }

  public String dumpRaml() {
    return api.dump(null);
  }

  public String getRamlV1() {
    if (keepApiBaseUri) {
      return dumpRaml();
    } else {
      String baseUriReplacement = getBaseUriReplacement(apiServer);
      return api.dump(baseUriReplacement);
    }
  }

  // resourcesRelativePath should not contain the console path
  public String getRamlV2(String resourceRelativePath) throws TypedException {
    String resourcePath = uriPathToResourcePath(resourceRelativePath);
    Path normalizedPath = Paths.get(resourcePath).normalize();
    // we don't want to expose something from the root. e.g. folder/some.xml/ it's ok, but some.xml should return 404
    if (normalizedPath.getNameCount() >= 1) {
      String normalized = normalizedPath.toString();
      if (apiResourcesRelativePath.equals(normalized)) {
        String rootRaml = dumpRaml();
        if (!keepApiBaseUri) {
          String baseUriReplacement = getBaseUriReplacement(apiServer);
          return UrlUtils.replaceBaseUri(rootRaml, baseUriReplacement);
        }
        return rootRaml;
      } else {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream apiResource = null;
        try {
          if (isSyncProtocol(apiResourcesRelativePath)) {
            ApiSyncResourceLoader loader = new ApiSyncResourceLoader(apiResourcesRelativePath);
            apiResource = loader.getResourceAsStream(normalized.substring(apiResourcesRelativePath.length()));
          } else {
            // this normalized path should be controlled carefully since can scan all the classpath.
            URL classpathResouce = Thread.currentThread().getContextClassLoader().getResource(normalized.replace("\\", "/"));
            if (classpathResouce != null) {
              String path = classpathResouce.getPath();
              // if is a console resource, the API raml or a resource of that raml
              if (CONSOLE_RESOURCE_PATTERN.asPredicate().test(path) ||
                  acceptedClasspathResources.stream().anyMatch(cr -> path.endsWith(cr.replace("\\", "/"))) ||
                  path.endsWith(api.getLocation())) {
                apiResource = classpathResouce.openStream();
              }
            }
          }

          if (apiResource != null) {
            StreamUtils.copyLarge(apiResource, baos);
            return baos.toString();
          }
        } catch (IOException e) {
          LOGGER.debug(e.getMessage());
          throw throwErrorType(new NotFoundException(resourceRelativePath), errorTypeRepository);
        } finally {
          IOUtils.closeQuietly(apiResource);
          IOUtils.closeQuietly(baos);
        }
      }
    }
    throw throwErrorType(new NotFoundException(resourceRelativePath), errorTypeRepository);
  }

  public String getAMFModel() {
    try {
      return replaceUriInAMFModel(apiServer).dumpAmf();
    } catch (IllegalStateException e) {
      return "";
    }
  }

  public synchronized void writeAMFModel(String url, OutputStream outputStream) {
    replaceUriInAMFModel(url).writeAMFModel(outputStream);
  }

  private AMFImpl replaceUriInAMFModel(String url) throws IllegalStateException {
    if (!AMF.equals(api.getType())) {
      throw new IllegalStateException("Trying to return AMF Model when RAML-Parser is being used");
    }
    AMFImpl amfApiSpec = ((AMFImpl) api);
    if (!keepApiBaseUri) {
      String baseUriReplacement = getBaseUriReplacement(url);
      amfApiSpec.updateBaseUri(baseUriReplacement);
    }
    return amfApiSpec;
  }


  public String getBaseUriReplacement(String apiServer) {
    return UrlUtils.getBaseUriReplacement(apiServer);
  }

  public boolean isRequestingRamlV1ForConsole(String listenerPath, String requestPath, String queryString, String method,
                                              String acceptHeader) {
    String postalistenerPath = UrlUtils.getListenerPath(listenerPath, requestPath);

    return (getApiVendor().equals(RAML_08) &&
        (postalistenerPath.equals(requestPath) || (postalistenerPath + "/").equals(requestPath)) &&
        ActionType.GET.toString().equals(method.toUpperCase()) &&
        (APPLICATION_RAML.equals(acceptHeader)
            || queryString.equals(RAML_QUERY_STRING)));
  }

  public boolean isRequestingRamlV2(String listenerPath, String requestPath, String queryString, String method) {
    String consolePath = UrlUtils.getListenerPath(listenerPath, requestPath);
    String resourcesFullPath = consolePath;
    if (!consolePath.endsWith("/")) {
      if (!apiResourcesRelativePath.startsWith("/")) {
        resourcesFullPath += "/";
      }
      resourcesFullPath += apiResourcesRelativePath;
    } else {
      if (apiResourcesRelativePath.startsWith("/") && apiResourcesRelativePath.length() > 1) {
        resourcesFullPath += apiResourcesRelativePath.substring(1);
      }
    }
    return getApiVendor().equals(RAML_10) && queryString.equals(RAML_QUERY_STRING)
        && ActionType.GET.toString().equals(method.toUpperCase())
        && requestPath.startsWith(resourcesFullPath);
  }

  private String uriPathToResourcePath(String resourceRelativePath) {
    // delete first slash
    if (resourceRelativePath.startsWith("/") && resourceRelativePath.length() > 1) {
      resourceRelativePath = resourceRelativePath.substring(1);
    }
    // delete querystring
    if (resourceRelativePath.contains("?raml")) {
      resourceRelativePath = resourceRelativePath.substring(0, resourceRelativePath.lastIndexOf('?'));
    }
    // delete last slash
    if (resourceRelativePath.endsWith("/") && resourceRelativePath.length() > 1) {
      resourceRelativePath = resourceRelativePath.substring(0, resourceRelativePath.length() - 1);
    }
    return Paths.get(resourceRelativePath).toString();
  }

  private String findRootRaml(String ramlLocation) {
    try {
      final URL url = new URL(ramlLocation);
      return url.toString();
    } catch (MalformedURLException e) {
      String[] startingLocations = new String[] {"api/", "", "api"};
      for (String start : startingLocations) {
        URL ramlLocationUrl = Thread.currentThread().getContextClassLoader().getResource(start + ramlLocation);
        if (ramlLocationUrl != null) {
          return start + ramlLocation;
        }
      }
    }
    return null;
  }

  public String getRootRamlLocationForV2() {
    return "this.location.href" + " + '" + apiResourcesRelativePath + "/?" + RAML_QUERY_STRING + "'";
  }

  public String getRootRamlLocationForV1() {
    return "this.location.href" + " + '" + "?" + RAML_QUERY_STRING + "'";
  }

  public String getSuccessStatusCode(Action action) {
    return action.getSuccessStatusCode();
  }

  public void setApiServer(String apiServer) {
    this.apiServer = apiServer;
  }
}
