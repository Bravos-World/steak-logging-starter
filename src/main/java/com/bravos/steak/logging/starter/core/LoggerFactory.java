package com.bravos.steak.logging.starter.core;

import com.bravos.steak.commonutils.shared.helper.Snowflake;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.*;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class LoggerFactory {

  final String serviceName;

  final KafkaTemplate<String, Object> kafkaTemplate;

  final ConcurrentHashMap<Class<?>, Logger> eventLoggers = new ConcurrentHashMap<>();

  final Snowflake snowflake;

  @Builder.Default
  int queueCapacity = 10000;

  final ExecutorService executorService = new ThreadPoolExecutor(
      2,
      6,
      60L, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(10000),
      new ThreadPoolExecutor.DiscardPolicy()
  );

  @Builder.Default
  final boolean infoEnabled = true;

  @Builder.Default
  final boolean debugEnabled = false;

  @Builder.Default
  final boolean errorEnabled = true;

  @Builder.Default
  final boolean warnEnabled = true;

  @Builder.Default
  String eventLogTopic = "event.log";

  public Logger getLogger(Class<?> clazz) {
    return eventLoggers.computeIfAbsent(clazz, this::createLogger);
  }

  private Logger createLogger(Class<?> clazz) {
    return Logger.builder()
        .loggerFactory(this)
        .clazz(clazz)
        .log(org.slf4j.LoggerFactory.getLogger(clazz))
        .build();
  }

}
