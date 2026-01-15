package com.bravos.steak.logging.starter.core;

import com.bravos.steak.commonutils.shared.helper.Snowflake;
import com.bravos.steak.logging.starter.model.EventLog;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.*;

/**
 * Factory for creating high-performance loggers with:
 * - Async appender using lock-free ring buffer
 * - Kafka integration for centralized logging
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class LoggerFactory {

  final String serviceName;
  final KafkaTemplate<String, Object> kafkaTemplate;
  final ConcurrentHashMap<Class<?>, Logger> eventLoggers = new ConcurrentHashMap<>();
  final Snowflake snowflake;
  final String eventLogTopic;
  final boolean infoEnabled;
  final boolean debugEnabled;
  final boolean errorEnabled;
  final boolean warnEnabled;
  final boolean consoleEnabled;
  final AsyncLogRingBuffer ringBuffer;

  final ExecutorService executorService = new ThreadPoolExecutor(
      2, 8,
      60L, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(10000),
      new ThreadPoolExecutor.DiscardPolicy()
  );

  private LoggerFactory(Builder builder) {
    this.serviceName = builder.serviceName;
    this.kafkaTemplate = builder.kafkaTemplate;
    this.snowflake = builder.snowflake;
    this.eventLogTopic = builder.eventLogTopic;
    this.infoEnabled = builder.infoEnabled;
    this.debugEnabled = builder.debugEnabled;
    this.errorEnabled = builder.errorEnabled;
    this.warnEnabled = builder.warnEnabled;
    this.consoleEnabled = builder.consoleEnabled;
    this.ringBuffer = new AsyncLogRingBuffer(builder.ringBufferSize, this::consumeLogEvent);
  }

  private void consumeLogEvent(AsyncLogRingBuffer.LogEvent event) {
    EventLog eventLog = event.toEventLog();
    kafkaTemplate.send(eventLogTopic, eventLog)
        .whenComplete((_, ex) -> {
          if (ex != null) {
            System.err.println("Failed to send event log to Kafka: " + ex.getMessage());
          }
        });
  }

  public Logger getLogger(Class<?> clazz) {
    return eventLoggers.computeIfAbsent(clazz, this::createLogger);
  }

  private Logger createLogger(Class<?> clazz) {
    return new Logger(this, clazz);
  }

  public void shutdown() {
    ringBuffer.shutdown();
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String serviceName;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private Snowflake snowflake;
    private String eventLogTopic = "event.log";
    private boolean infoEnabled = true;
    private boolean debugEnabled = false;
    private boolean errorEnabled = true;
    private boolean warnEnabled = true;
    private boolean consoleEnabled = true;
    private int ringBufferSize = 1024 * 16; // 16K entries

    public Builder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder kafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) {
      this.kafkaTemplate = kafkaTemplate;
      return this;
    }

    public Builder snowflake(Snowflake snowflake) {
      this.snowflake = snowflake;
      return this;
    }

    public Builder eventLogTopic(String eventLogTopic) {
      this.eventLogTopic = eventLogTopic;
      return this;
    }

    public Builder infoEnabled(boolean infoEnabled) {
      this.infoEnabled = infoEnabled;
      return this;
    }

    public Builder debugEnabled(boolean debugEnabled) {
      this.debugEnabled = debugEnabled;
      return this;
    }

    public Builder errorEnabled(boolean errorEnabled) {
      this.errorEnabled = errorEnabled;
      return this;
    }

    public Builder warnEnabled(boolean warnEnabled) {
      this.warnEnabled = warnEnabled;
      return this;
    }

    public Builder consoleEnabled(boolean consoleEnabled) {
      this.consoleEnabled = consoleEnabled;
      return this;
    }

    public Builder ringBufferSize(int ringBufferSize) {
      this.ringBufferSize = ringBufferSize;
      return this;
    }

    public LoggerFactory build() {
      return new LoggerFactory(this);
    }
  }

}
