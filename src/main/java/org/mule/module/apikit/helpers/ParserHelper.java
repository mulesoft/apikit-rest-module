/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.helpers;

import org.mule.apikit.model.api.ApiReference;
import org.mule.parser.service.ParserMode;
import org.mule.parser.service.ParserService;
import org.mule.parser.service.result.ParseResult;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerConfig;
import org.mule.runtime.api.scheduler.SchedulerService;

import java.util.function.Function;

public class ParserHelper {

  public static final String SCHEDULER_NAME = "AMF-SCHEDULER";
  public static final int QUEUE_SIZE = Integer.MAX_VALUE;
  public static final int MAX_CONCURRENT_TASKS = Runtime.getRuntime().availableProcessors();

  public static final SchedulerConfig SCHEDULER_CONFIG = SchedulerConfig.config()
      .withName(SCHEDULER_NAME)
      .withMaxConcurrentTasks(MAX_CONCURRENT_TASKS);

  private final SchedulerService schedulerService;
  private final String rootRamlLocation;
  private final ParserMode parserMode;

  public ParserHelper(SchedulerService schedulerService, String rootRamlLocation, ParserMode parserMode) {
    this.schedulerService = schedulerService;
    this.rootRamlLocation = rootRamlLocation;
    this.parserMode = parserMode;
  }

  public <T> T executeWithScheduler(Function<ParseResult, T> function) {
    if(schedulerService != null) {
      final Scheduler scheduler = schedulerService.customScheduler(SCHEDULER_CONFIG, QUEUE_SIZE);
      final ParserService parserService = new ParserService(scheduler);
      final ParseResult parseResult = parserService.parse(ApiReference.create(rootRamlLocation), parserMode);
      T result = function.apply(parseResult);
      scheduler.shutdownNow();
      return result;
    }else{
      final ParserService parserService = new ParserService();
      final ParseResult parseResult = parserService.parse(ApiReference.create(rootRamlLocation), parserMode);
      T result = function.apply(parseResult);
      return result;
    }
  }

}
