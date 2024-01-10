/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.deserializing;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.net.MediaType;
import org.apache.commons.lang.math.NumberUtils;
import org.mule.module.apikit.deserializing.MimeType.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mule.module.apikit.api.deserializing.ArrayHeaderDelimiter.COMMA;

public class MimeMatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(MimeMatcher.class);

  protected static float getQualityParam(MimeType mime, float defaultValue) {
    return mime.getParameters().stream()
            .filter(param -> "q".equals(param.getAttribute()))
            .map(Parameter::getValue)
            .map(q -> NumberUtils.toFloat(q, -1))
            .filter(f -> f < 0 || f > 1)
            .findFirst()
            .orElse(defaultValue);

  }

  protected static class FitnessAndQuality implements Comparable<FitnessAndQuality> {

    int fitness;
    float quality;
    MimeType mimeType;

    public FitnessAndQuality(int fitness, float quality, MimeType mimeType) {
      this.fitness = fitness;
      this.quality = quality;
    }

    @Override
    public int compareTo(FitnessAndQuality o) {
      int fitnessCompare = Float.compare(fitness, o.fitness);
      return fitnessCompare == 0
              ?  Float.compare(quality, o.quality)
              : fitnessCompare;
    }
  }

  /**
   * Find the best match for a given mimeType against a list of media_ranges that have already been parsed by
   * MimeParse.parseMediaRange(). Returns a tuple of the fitness value and the value of the 'q' quality parameter of the best
   * match, or (-1, 0) if no match was found. Just as for quality_parsed(), 'parsed_ranges' must be a list of parsed media ranges.
   *
   * @param target
   * @param parsedRanges
   */
  protected static FitnessAndQuality fitnessAndQualityParsed(MimeType target,
                                                             Collection<MimeType> parsedRanges, float defaultQuality) {
    int bestFitness = -1;
    float bestFitQ = 0;
    String targetType = target.getType();
    String targetSubtype = target.getSubtype();
    Map<String, Set<String>> targetParams = paramListAsMap(target.getParameters());

    for (MimeType range : parsedRanges) {
      String rangeType = range.getType();
      String rangeSubtype = range.getSubtype();
      Map<String, Set<String>> rangeParams = paramListAsMap(range.getParameters());
      if (isCompatible(targetType, rangeType) && isCompatible(targetSubtype, rangeSubtype)) {
        for (Entry<String, Set<String>> entry : targetParams.entrySet()) {
          String k = entry.getKey();
          Set<String> v = entry.getValue();
          long paramMatches = rangeParams.get(k).stream().filter(v::contains).count();
          int fitness = rangeType.equalsIgnoreCase(targetType) ? 100 : 0;
          fitness += rangeSubtype.equalsIgnoreCase(targetSubtype) ? 10 : 0;
          fitness += paramMatches;
          if (fitness > bestFitness) {
            bestFitness = fitness;
            bestFitQ = "*".equals(rangeType) && "*".equals(rangeSubtype)
                    ? getQualityParam(target, defaultQuality)
                    : getQualityParam(range, 0);
          }
        }
      }
    }
    return new FitnessAndQuality(bestFitness, bestFitQ, target);
  }

  private static boolean isCompatible(String a, String b) {
    return a.equalsIgnoreCase(b) || "*".equals(b) || "*".equals(a);
  }

  private static Map<String, Set<String>> paramListAsMap(List<Parameter> parameters) {
    return parameters.stream()
            .filter(v -> !"q".equals(v.getAttribute()))
            .collect(Collectors.groupingBy(Parameter::getAttribute,
                    Collectors.mapping(Parameter::getValue, Collectors.toSet())));
  }

  private static MediaType asMediaType(MimeType mimeType) {
    return MediaType.create(mimeType.getType(), mimeType.getSubtype())
            .withParameters(ImmutableMultimap.copyOf(mimeType.getParameters()));
  }

  /**
   * Takes a list of supportedRepresentations mime-types and finds the best match for all the media-ranges listed in header. The
   * value of header must be a string that conforms to the format of the HTTP Accept: header. The value of
   * 'supportedRepresentations' is a list of mime-types.
   * <p/>
   * MimeParse.bestMatch(Arrays.asList(new String[]{"application/xbel+xml", "text/xml"}), "text/*;q=0.5,*; q=0.1") 'text/xml'
   *
   * @param supportedRepresentations
   * @param header
   * @return
   */
  public static Optional<MediaType> bestMatchForAcceptHeader(List<String> supportedRepresentations, String header) {
    List<MimeType> parseResults;
    try {
      parseResults = MimeType.listFrom(header, COMMA.getDelimiterChar());
    } catch (MimeType.MimeTypeParseException e) {
      LOGGER.warn("Failed to parse user-provided MimeType list: {}", header, e);
      return Optional.empty();
    }

    List<MimeType> supported = supportedRepresentations.stream()
            .flatMap(representation -> {
              try {
                return Stream.of(MimeType.from(representation));
              } catch (MimeType.MimeTypeParseException e) {
                LOGGER.warn("Failed to parse application-provided MimeType: {}", representation, e);
                return Stream.empty();
              }
            })
            .collect(Collectors.toList());

    Stream<FitnessAndQuality> first = supported.stream()
            .map(representation -> fitnessAndQualityParsed(representation, parseResults, 1))
            .limit(1);
    Stream<FitnessAndQuality> rest = supported.stream()
            .skip(1)
            .map(representation -> fitnessAndQualityParsed(representation, parseResults, 0.5f));
    return Stream.concat(first, rest)
            .min(FitnessAndQuality::compareTo)
            .map(best -> asMediaType(best.mimeType));
  }
}
