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

  public void info(String eventName, String message, Map<String, Object> metadata) {
    if (this.loggerFactory.isInfoEnabled()) {
      String threadName = Thread.currentThread().getName();
      loggerFactory.getExecutorService().submit(() -> {
        String messageWithClass = String.format("[%s] [%s] %s", clazz.getName(), threadName, message);
        EventLog eventLog = EventLog.builder()
            .id(this.loggerFactory.getSnowflake().next())
            .level("INFO")
            .eventName(eventName)
            .message(messageWithClass)
            .service(this.loggerFactory.getServiceName())
            .metadata(metadata)
            .timestamp(DateTimeHelper.currentTimeMillis())
            .build();
        System.out.println(messageWithClass);
        this.sendMessageToKafka(eventLog);
      });
    }
  }

  public void info(String eventName, String message, SensitiveData sensitiveData) {
    if (this.loggerFactory.isInfoEnabled()) {
      this.infoWithSensitiveData(eventName, message, sensitiveData);
    }
  }

  @MutateSensitiveData
  private void infoWithSensitiveData(String eventName, String message, SensitiveData sensitiveData) {
    this.info(eventName, message, sensitiveData.getMutatedData());
  }

  public void info(String eventName, String message) {
    this.info(eventName, message, Map.of());
  }

  public void error(String eventName, String message, Throwable throwable, Map<String, Object> metadata) {
    if (this.loggerFactory.isErrorEnabled()) {
      String threadName = Thread.currentThread().getName();
      loggerFactory.getExecutorService().submit(() -> {
        String messageWithClass = String.format("[%s] [%s] %s", clazz.getName(), threadName, message);
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
        System.err.println(messageWithClass);
        this.sendMessageToKafka(eventLog);
      });
    }
  }

  public void error(String eventName, String message, Throwable throwable, SensitiveData sensitiveData) {
    if (this.loggerFactory.isErrorEnabled()) {
      this.errorWithSensitiveData(eventName, message, throwable, sensitiveData);
    }
  }

  @MutateSensitiveData
  private void errorWithSensitiveData(String eventName, String message, Throwable throwable, SensitiveData sensitiveData) {
    this.error(eventName, message, throwable, sensitiveData.getMutatedData());
  }

  public void error(String eventName, String message) {
    this.error(eventName, message, null, Map.of());
  }

  public void error(String eventName, String message, Throwable throwable) {
    this.error(eventName, message, throwable, Map.of());
  }

  public void debug(String eventName, String message, Map<String, Object> metadata) {
    if (this.loggerFactory.isDebugEnabled()) {
      String threadName = Thread.currentThread().getName();
      loggerFactory.getExecutorService().submit(() -> {
        String messageWithClass = String.format("[%s] [%s] %s", clazz.getName(), threadName, message);
        EventLog eventLog = EventLog.builder()
            .id(this.loggerFactory.getSnowflake().next())
            .level("DEBUG")
            .eventName(eventName)
            .message(messageWithClass)
            .service(this.loggerFactory.getServiceName())
            .metadata(metadata)
            .timestamp(DateTimeHelper.currentTimeMillis())
            .build();
        System.out.println(messageWithClass);
        this.sendMessageToKafka(eventLog);
      });
    }
  }

  public void debug(String eventName, String message) {
    this.debug(eventName, message, Map.of());
  }

  public void debug(String eventName, String message, SensitiveData sensitiveData) {
    if (this.loggerFactory.isDebugEnabled()) {
      this.debugWithSensitiveData(eventName, message, sensitiveData);
    }
  }

  @MutateSensitiveData
  private void debugWithSensitiveData(String eventName, String message, SensitiveData sensitiveData) {
    this.debug(eventName, message, sensitiveData.getMutatedData());
  }

  public void warn(String eventName, String message, Map<String, Object> metadata) {
    if (this.loggerFactory.isWarnEnabled()) {
      String threadName = Thread.currentThread().getName();
      loggerFactory.getExecutorService().submit(() -> {
        String messageWithClass = String.format("[%s] [%s] %s", clazz.getName(), threadName, message);
        EventLog eventLog = EventLog.builder()
            .id(this.loggerFactory.getSnowflake().next())
            .level("WARN")
            .eventName(eventName)
            .message(messageWithClass)
            .service(this.loggerFactory.getServiceName())
            .metadata(metadata)
            .timestamp(DateTimeHelper.currentTimeMillis())
            .build();
        this.sendMessageToKafka(eventLog);
        System.out.println(messageWithClass);
      });
    }
  }

  public void warn(String eventName, String message) {
    this.warn(eventName, message, Map.of());
  }

  public void warn(String eventName, String message, SensitiveData sensitiveData) {
    if (this.loggerFactory.isWarnEnabled()) {
      this.warnWithSensitiveData(eventName, message, sensitiveData);
    }
  }

  @MutateSensitiveData
  private void warnWithSensitiveData(String eventName, String message, SensitiveData sensitiveData) {
    this.warn(eventName, message, sensitiveData.getMutatedData());
  }

  private void sendMessageToKafka(EventLog eventLog) {
    this.loggerFactory.getKafkaTemplate()
        .send(this.loggerFactory.getEventLogTopic(), eventLog)
        .whenComplete((_, ex) -> {
          if (ex != null) {
            System.err.println("Failed to send event log to Kafka: " + ex.getMessage());
          }
        });
  }

}
