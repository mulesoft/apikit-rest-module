/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.api;

import org.apache.commons.lang3.StringUtils;
import org.mule.module.apikit.uri.URICoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.getProperty;

public class UrlUtils {

  private static final String BIND_TO_ALL_INTERFACES = "0.0.0.0";
  public static final String FULL_DOMAIN = "fullDomain";
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private static final Set<Character> ESCAPE_CHARS = new HashSet<>(Arrays.asList('/', '{', '}'));

  private UrlUtils() {}

  /**
   * @param baseAndApiPath http listener base path, example /api/*
   * @param requestPath    example /api/endpoint
   * @return index of character in requestPath corresponding to the last slash found in base path
   */
  private static int getEndOfBasePathIndex(String baseAndApiPath, String requestPath) {
    int amountOfSlashesInBasePath = 0;
    for (int i = 0; i < baseAndApiPath.length(); i++) {
      if (baseAndApiPath.charAt(i) == '/') {
        amountOfSlashesInBasePath++;
      }
    }
    int amountOfSlashesInRequestPath = 0;
    int character = 0;
    for (; character < requestPath.length() && amountOfSlashesInRequestPath < amountOfSlashesInBasePath; character++) {
      if (requestPath.charAt(character) == '/') {
        amountOfSlashesInRequestPath++;
      }
    }
    return character;
  }

  public static String encode(String url) {
    return URICoder.encode(url, ESCAPE_CHARS);
  }

  /**
   * @param baseAndApiPath http listener base path, example /api/*
   * @param requestPath example /api/endpoint
   * @return a String that represent the relative path between @baseAndApiPath and @requestPath,
   * example : /endpoint
   */
  public static String getRelativePath(String baseAndApiPath, String requestPath) {
    int slashLastPosition = getEndOfBasePathIndex(baseAndApiPath, requestPath);
    return slashLastPosition == 0 || slashLastPosition >= requestPath.length() ? "/"
        : requestPath.substring(slashLastPosition - 1);
  }

  public static String getListenerPath(String listenerPath, String requestPath) {

    if (!listenerPath.startsWith("/")) {
      listenerPath = "/" + listenerPath;
    }
    if (!requestPath.startsWith("/")) {
      requestPath = "/" + requestPath;
    }
    int slashesAmount = 0;
    for (int i = 0; i < listenerPath.length(); i++) {
      if (listenerPath.charAt(i) == '/') {
        slashesAmount++;
      }
    }
    String[] split = requestPath.split("/");
    String result = "";
    if (split.length == 0) {
      return "/";
    }
    if (split.length == 1 && split[0].equals("")) {
      return "/";
    }
    for (int i = 0; i < slashesAmount; i++) {
      if (!split[i].equals("")) {
        result += "/" + split[i];
      }
    }
    return result;
  }


  public static String getBasePath(String baseAndApiPath, String requestPath) {
    int character = getEndOfBasePathIndex(baseAndApiPath, requestPath);
    return requestPath.substring(0, character);
  }

  public static String replaceBaseUri(String raml, String newBaseUri) {
    if (newBaseUri != null) {
      return replaceBaseUri(raml, ".*$", newBaseUri);
    }
    return raml;
  }

  private static String replaceBaseUri(String raml, String regex, String replacement) {
    String[] split = raml.split("\n");
    boolean found = false;
    for (int i = 0; i < split.length; i++) {
      if (split[i].startsWith("baseUri: ")) {
        found = true;
        split[i] = split[i].replaceFirst(regex, replacement);
        if (!split[i].contains("baseUri: ")) {
          split[i] = "baseUri: " + split[i];
        }
      }
    }
    if (!found) {
      for (int i = 0; i < split.length; i++) {
        if (split[i].startsWith("title:")) {
          if (replacement.contains("baseUri:")) {
            split[i] = split[i] + "\n" + replacement;
          } else {
            split[i] = split[i] + "\n" + "baseUri: " + replacement;
          }
        }
      }
    }
    return StringUtils.join(split, "\n");
  }

  /**
   * Creates URL where the server must redirect the client
   *
   * @return The redirect URL
   */
  public static String getRedirectLocation(String scheme, String remoteAddress, String requestPath,
                                           String queryString) {
    String redirectLocation = scheme + "://" + remoteAddress + requestPath + "/";

    if (StringUtils.isNotEmpty(queryString)) {
      redirectLocation += "?" + queryString;
    }

    return redirectLocation;
  }

  public static String getBaseUriReplacement(String apiServer) {
    return replaceHostInURL(apiServer, null);
  }

  /**
   * @param routerURL url where router is listening for requests
   * @param consoleRequestHost example : <IP>:<PORT> or <protocol>://<IP>:<PORT>,
   * this parameter is used if 'fullDomain' System property is not present
   * @return routerURL with host replaced
   */
  public static String replaceHostInURL(String routerURL, String consoleRequestHost) {
    if (routerURL == null) {
      return null;
    }
    if (!routerURL.contains(BIND_TO_ALL_INTERFACES)) {
      return routerURL;
    }

    String hostToReplace = getProperty(FULL_DOMAIN) != null ? getProperty(FULL_DOMAIN) : consoleRequestHost;
    if (hostToReplace == null) {
      return routerURL.replace(BIND_TO_ALL_INTERFACES, "localhost");
    }
    try {
      String protocol = routerURL.contains(HTTPS) ? HTTPS : HTTP;
      String path = new URL(routerURL).getPath();
      path = hostToReplace.endsWith("/") && path.length() > 0 ? path.substring(1) : path;
      if (hostToReplace.contains("://")) {
        return hostToReplace + path;
      }
      return protocol + hostToReplace + path;
    } catch (MalformedURLException e) {
      return routerURL;
    }

  }

}
