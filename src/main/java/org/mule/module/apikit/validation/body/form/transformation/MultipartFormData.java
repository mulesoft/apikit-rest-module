/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.api.metadata.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mule.module.apikit.StreamUtils.BUFFER_SIZE;

public class MultipartFormData {
  private static Pattern NAME_PATTERN = Pattern.compile("Content-Disposition:\\s*form-data;[^\\n]*\\sname=([^\\n;]*?)[;\\n\\s]");
  private static Pattern CONTENT_TYPE_PATTERN = Pattern.compile("Content-Type:\\s*([^\\n;]*?)[;\\n\\s]");
  private MultipartStream multipartStream;
  private MultipartEntityBuilder multipartEntityBuilder;
  private Boolean defaultsAdded = false;

  public MultipartFormData(InputStream inputStream, byte[] boundary){
    multipartStream = new MultipartStream(inputStream, boundary, BUFFER_SIZE,null);
    multipartEntityBuilder = MultipartEntityBuilder.create();
  }

  public Map<String, MultipartFormDataParameter> getFormDataParameters() throws InvalidFormParameterException {
    Map<String, MultipartFormDataParameter> multiMapParameters= new HashMap<>();
    try {
      boolean nextPart = multipartStream.skipPreamble();
      while (nextPart) {
        String headers = multipartStream.readHeaders();
        String name = getName(headers);
        MediaType mediaType = getContentType(headers);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        multipartStream.readBodyData(baos);
        byte[] buf = baos.toByteArray();
        multipartEntityBuilder.addBinaryBody(name,buf);
        multiMapParameters.put(name,new MultipartFormDataParameter( new ByteArrayInputStream(buf),mediaType));
        nextPart = multipartStream.readBoundary();
      }

    } catch (Exception e){
      throw new InvalidFormParameterException(e);
    }
    return multiMapParameters;
  }

  private String getName(String headers) throws InvalidFormParameterException {
    Matcher matcher = NAME_PATTERN.matcher(headers);
    if (!matcher.find()){
      throw new InvalidFormParameterException("Unable to get name from form-data");
    }

    return matcher.group(1).replace("\"","").replace("'","");
  }

  private MediaType getContentType(String headers){
    Matcher matcher = CONTENT_TYPE_PATTERN.matcher(headers);
    if (!matcher.find()){
      return MediaType.TEXT;
    }
    return MediaType.parse(matcher.group(1));
  }
  public void addDefault(String key,String value){
    defaultsAdded = true;
    multipartEntityBuilder.addTextBody(key,value);
  }

  public InputStream build() throws InvalidFormParameterException{
    try {
      return multipartEntityBuilder.build().getContent();
    } catch (IOException e) {
      throw new InvalidFormParameterException(e);
    }
  }

  public boolean areDefaultsAdded(){
    return defaultsAdded;
  }
}
