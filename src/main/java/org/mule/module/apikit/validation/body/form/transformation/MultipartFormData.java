package org.mule.module.apikit.validation.body.form.transformation;

import org.apache.commons.fileupload.MultipartStream;
import org.mule.module.apikit.api.exception.InvalidFormParameterException;
import org.mule.runtime.api.metadata.MediaType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MultipartFormData {
  private final MultipartStream multipartStream;
  private static final int BUF_SIZE = 4096;
  private static final Pattern NAME_PATTERN = Pattern.compile("Content-Disposition:\\s*form-data;[^\\n]*\\sname=([^\\n;]*?)[;\\n\\s]");
  private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("Content-Type:\\s*([^\\n;]*?)[;\\n\\s]");

  public MultipartFormData(InputStream inputStream, byte[] boundary){
    multipartStream = new MultipartStream(inputStream, boundary, BUF_SIZE,null);
  }

  public Map<String, MultipartFormDataParameter> getFormDataParameters() throws InvalidFormParameterException {
    Map<String, MultipartFormDataParameter> multiMapParameters= new HashMap<>();
    try {
      boolean nextPart = multipartStream.skipPreamble();
      while (nextPart) {
        String headers = multipartStream.readHeaders();
        String name = getName(headers);
        MediaType mediaType = getContentType(headers);
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        multipartStream.readBodyData(out);
        out.close();
        multiMapParameters.put(name,new MultipartFormDataParameter( in ,mediaType));
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
}
