package com.bravos.steak.logging.starter.core;

import com.bravos.steak.commonutils.shared.helper.DateTimeHelper;
import com.bravos.steak.logging.starter.annotation.MutateSensitiveData;
import com.bravos.steak.logging.starter.model.EventLog;
import lombok.Builder;

import java.util.Map;

@Builder
public class Logger {

  private final LoggerFactory loggerFactory;
  private final Class<?> clazz;
  private final org.slf4j.Logger log;
  private final MutateHelperSensitive mutateHelperSensitive = new MutateHelperSensitive(this);

  private void infoHandler(String eventName, String message, Map<String, Object> metadata) {
    String messageWithClass = String.format("[%s] %s", clazz.getName(), message);
    EventLog eventLog = EventLog.builder()
        .id(this.loggerFactory.getSnowflake().next())
        .level("INFO")
        .eventName(eventName)
        .message(messageWithClass)
        .service(this.loggerFactory.getServiceName())
        .metadata(metadata)
        .timestamp(DateTimeHelper.currentTimeMillis())
        .build();
    log.info(message);
    this.sendMessageToKafka(eventLog);
  }

  public void info(String eventName, String message, Map<String, Object> metadata) {
    if (this.loggerFactory.isInfoEnabled()) {
      Map<String, Object> immutableMetadata = Map.copyOf(metadata);
      this.loggerFactory.getExecutorService().submit(() -> this.infoHandler(eventName, message, immutableMetadata));
    }
  }

  public void info(String eventName, String message, SensitiveData sensitiveData) {
    if (this.loggerFactory.isInfoEnabled()) {
      this.loggerFactory.getExecutorService().submit(() -> this.mutateHelperSensitive.infoWithSensitiveData(eventName, message, sensitiveData));
    }
  }

  public void info(String eventName, String message) {
    if (this.loggerFactory.isInfoEnabled()) {
      this.infoHandler(eventName, message, Map.of());
    }
  }

  private void errorHandler(String eventName, String message, Throwable throwable, Map<String, Object> metadata) {
    String messageWithClass = String.format("[%s] %s", clazz.getName(), message);
    EventLog eventLog = EventLog.builder()
        .id(this.loggerFactory.getSnowflake().next())
        .level("ERROR")
        .eventName(eventName)
        .message(messageWithClass)
        .service(this.loggerFactory.getServiceName())
        .exceptionTrace(throwable != null ? throwable.toString() : null)
        .metadata(metadata)
        .timestamp(DateTimeHelper.currentTimeMillis())
        .build();
    log.error(message, throwable);
    this.sendMessageToKafka(eventLog);
  }

  public void error(String eventName, String message, Throwable throwable, Map<String, Object> metadata) {
    if (this.loggerFactory.isErrorEnabled()) {
      Map<String, Object> immutableMetadata = Map.copyOf(metadata);
      this.loggerFactory.getExecutorService().submit(() -> this.errorHandler(eventName, message, throwable, immutableMetadata));
    }
  }

  public void error(String eventName, String message, Throwable throwable, SensitiveData sensitiveData) {
    if (this.loggerFactory.isErrorEnabled()) {
      this.loggerFactory.getExecutorService().submit(() -> this.mutateHelperSensitive.errorWithSensitiveData(eventName, message, throwable, sensitiveData));
    }
  }

  public void error(String eventName, String message) {
    this.error(eventName, message, null, Map.of());
  }

  public void error(String eventName, String message, Throwable throwable) {
    this.error(eventName, message, throwable, Map.of());
  }

  private void debugHandler(String eventName, String message, Map<String, Object> metadata) {
    String messageWithClass = String.format("[%s] %s", clazz.getName(), message);
    EventLog eventLog = EventLog.builder()
        .id(this.loggerFactory.getSnowflake().next())
        .level("DEBUG")
        .eventName(eventName)
        .message(messageWithClass)
        .service(this.loggerFactory.getServiceName())
        .metadata(metadata)
        .timestamp(DateTimeHelper.currentTimeMillis())
        .build();
    log.debug(message);
    this.sendMessageToKafka(eventLog);
  }

  public void debug(String eventName, String message, Map<String, Object> metadata) {
    if (this.loggerFactory.isDebugEnabled()) {
      Map<String, Object> immutableMetadata = Map.copyOf(metadata);
      this.loggerFactory.getExecutorService().submit(() -> this.debugHandler(eventName, message, immutableMetadata));
    }
  }

  public void debug(String eventName, String message) {
    this.debug(eventName, message, Map.of());
  }

  public void debug(String eventName, String message, SensitiveData sensitiveData) {
    if (this.loggerFactory.isDebugEnabled()) {
      this.loggerFactory.getExecutorService().submit(() -> this.mutateHelperSensitive.debugWithSensitiveData(eventName, message, sensitiveData));
    }
  }

  private void warnHandler(String eventName, String message, Map<String, Object> metadata) {
    String messageWithClass = String.format("[%s] %s", clazz.getName(), message);
    EventLog eventLog = EventLog.builder()
        .id(this.loggerFactory.getSnowflake().next())
        .level("WARN")
        .eventName(eventName)
        .message(messageWithClass)
        .service(this.loggerFactory.getServiceName())
        .metadata(metadata)
        .timestamp(DateTimeHelper.currentTimeMillis())
        .build();
    log.warn(message);
    this.sendMessageToKafka(eventLog);
  }

  public void warn(String eventName, String message, Map<String, Object> metadata) {
    if (this.loggerFactory.isWarnEnabled()) {
      Map<String, Object> immutableMetadata = Map.copyOf(metadata);
      this.loggerFactory.getExecutorService().submit(() -> this.warnHandler(eventName, message, immutableMetadata));
    }
  }

  public void warn(String eventName, String message) {
    this.warn(eventName, message, Map.of());
  }

  public void warn(String eventName, String message, SensitiveData sensitiveData) {
    if (this.loggerFactory.isWarnEnabled()) {
      this.loggerFactory.getExecutorService().submit(() -> this.mutateHelperSensitive.warnWithSensitiveData(eventName, message, sensitiveData));
    }
  }

  private void sendMessageToKafka(EventLog eventLog) {
    this.loggerFactory.getKafkaTemplate()
        .send(this.loggerFactory.getEventLogTopic(), eventLog)
        .whenComplete((_, ex) -> {
          if (ex != null) {
            log.error("Failed to send event log to Kafka", ex);
          }
        });
  }

  private record MutateHelperSensitive(Logger logger) {

    @MutateSensitiveData
    public void infoWithSensitiveData(String eventName, String message, SensitiveData sensitiveData) {
      logger.infoHandler(eventName, message, sensitiveData.getMutatedData());
    }

    @MutateSensitiveData
    public void warnWithSensitiveData(String eventName, String message, SensitiveData sensitiveData) {
      logger.warnHandler(eventName, message, sensitiveData.getMutatedData());
    }

    @MutateSensitiveData
    public void debugWithSensitiveData(String eventName, String message, SensitiveData sensitiveData) {
      logger.debugHandler(eventName, message, sensitiveData.getMutatedData());
    }

    @MutateSensitiveData
    public void errorWithSensitiveData(String eventName, String message, Throwable throwable, SensitiveData sensitiveData) {
      logger.errorHandler(eventName, message, throwable, sensitiveData.getMutatedData());
    }

  }

}
