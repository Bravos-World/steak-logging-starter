package com.bravos.steak.logging.starter.core;

import com.bravos.steak.commonutils.shared.helper.DateTimeHelper;
import com.bravos.steak.logging.starter.annotation.MutateSensitiveData;
import com.bravos.steak.logging.starter.core.LogMessageFormatter.FormattedResult;
import com.bravos.steak.security.starter.context.RequestContext;
import com.bravos.steak.security.starter.context.RequestContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * High-performance logger with:
 * - SLF4J-style {} placeholder formatting
 * - Auto-detection of Throwable at end of arguments
 * - Zero-allocation message formatting (ThreadLocal StringBuilder)
 * - Async appender using lock-free ring buffer
 */
public final class Logger {

  private static final Map<String, Object> EMPTY_METADATA = Collections.emptyMap();
  private static final String LEVEL_INFO = "INFO";
  private static final String LEVEL_WARN = "WARN";
  private static final String LEVEL_ERROR = "ERROR";
  private static final String LEVEL_DEBUG = "DEBUG";

  private final LoggerFactory loggerFactory;
  private final Class<?> clazz;
  private final MutateHelperSensitive mutateHelperSensitive;

  Logger(LoggerFactory loggerFactory, Class<?> clazz) {
    this.loggerFactory = loggerFactory;
    this.clazz = clazz;
    this.mutateHelperSensitive = new MutateHelperSensitive(this);
  }

  // ==================== INFO ====================

  /**
   * Log info message with SLF4J-style formatting.
   * Supports {} placeholders and auto-detects Throwable at end of args.
   *
   * @param eventName the event name
   * @param message   the message pattern with {} placeholders
   * @param args      the arguments (last arg can be Throwable)
   */
  public void info(String eventName, String message, Object... args) {
    if (!loggerFactory.isInfoEnabled()) return;
    FormattedResult result = LogMessageFormatter.format(message, args);
    publishAsync(LEVEL_INFO, eventName, result.message(), result.throwable(), EMPTY_METADATA);
  }

  public void info(String eventName, String message) {
    if (!loggerFactory.isInfoEnabled()) return;
    publishAsync(LEVEL_INFO, eventName, message, null, EMPTY_METADATA);
  }

  public void info(String eventName, String message, Map<String, Object> metadata) {
    if (!loggerFactory.isInfoEnabled()) return;
    publishAsync(LEVEL_INFO, eventName, message, null, copyMetadata(metadata));
  }

  public void info(String eventName, String message, SensitiveData sensitiveData) {
    if (!loggerFactory.isInfoEnabled()) return;
    final String traceId = getTraceId();
    loggerFactory.getExecutorService().submit(() ->
        mutateHelperSensitive.infoWithSensitiveData(traceId, eventName, message, sensitiveData));
  }

  // ==================== WARN ====================

  /**
   * Log warn message with SLF4J-style formatting.
   */
  public void warn(String eventName, String message, Object... args) {
    if (!loggerFactory.isWarnEnabled()) return;
    FormattedResult result = LogMessageFormatter.format(message, args);
    publishAsync(LEVEL_WARN, eventName, result.message(), result.throwable(), EMPTY_METADATA);
  }

  public void warn(String eventName, String message) {
    if (!loggerFactory.isWarnEnabled()) return;
    publishAsync(LEVEL_WARN, eventName, message, null, EMPTY_METADATA);
  }

  public void warn(String eventName, String message, Map<String, Object> metadata) {
    if (!loggerFactory.isWarnEnabled()) return;
    publishAsync(LEVEL_WARN, eventName, message, null, copyMetadata(metadata));
  }

  public void warn(String eventName, String message, SensitiveData sensitiveData) {
    if (!loggerFactory.isWarnEnabled()) return;
    final String traceId = getTraceId();
    loggerFactory.getExecutorService().submit(() ->
        mutateHelperSensitive.warnWithSensitiveData(traceId, eventName, message, sensitiveData));
  }

  // ==================== ERROR ====================

  /**
   * Log error message with SLF4J-style formatting.
   */
  public void error(String eventName, String message, Object... args) {
    if (!loggerFactory.isErrorEnabled()) return;
    FormattedResult result = LogMessageFormatter.format(message, args);
    publishAsync(LEVEL_ERROR, eventName, result.message(), result.throwable(), EMPTY_METADATA);
  }

  public void error(String eventName, String message) {
    if (!loggerFactory.isErrorEnabled()) return;
    publishAsync(LEVEL_ERROR, eventName, message, null, EMPTY_METADATA);
  }

  public void error(String eventName, String message, Throwable throwable) {
    if (!loggerFactory.isErrorEnabled()) return;
    publishAsync(LEVEL_ERROR, eventName, message, throwable, EMPTY_METADATA);
  }

  public void error(String eventName, String message, Throwable throwable, Map<String, Object> metadata) {
    if (!loggerFactory.isErrorEnabled()) return;
    publishAsync(LEVEL_ERROR, eventName, message, throwable, copyMetadata(metadata));
  }

  public void error(String eventName, String message, Throwable throwable, SensitiveData sensitiveData) {
    if (!loggerFactory.isErrorEnabled()) return;
    final String traceId = getTraceId();
    loggerFactory.getExecutorService().submit(() ->
        mutateHelperSensitive.errorWithSensitiveData(traceId, eventName, message, throwable, sensitiveData));
  }

  // ==================== DEBUG ====================

  /**
   * Log debug message with SLF4J-style formatting.
   */
  public void debug(String eventName, String message, Object... args) {
    if (!loggerFactory.isDebugEnabled()) return;
    FormattedResult result = LogMessageFormatter.format(message, args);
    publishAsync(LEVEL_DEBUG, eventName, result.message(), result.throwable(), EMPTY_METADATA);
  }

  public void debug(String eventName, String message) {
    if (!loggerFactory.isDebugEnabled()) return;
    publishAsync(LEVEL_DEBUG, eventName, message, null, EMPTY_METADATA);
  }

  public void debug(String eventName, String message, Map<String, Object> metadata) {
    if (!loggerFactory.isDebugEnabled()) return;
    publishAsync(LEVEL_DEBUG, eventName, message, null, copyMetadata(metadata));
  }

  public void debug(String eventName, String message, SensitiveData sensitiveData) {
    if (!loggerFactory.isDebugEnabled()) return;
    final String traceId = getTraceId();
    loggerFactory.getExecutorService().submit(() ->
        mutateHelperSensitive.debugWithSensitiveData(traceId, eventName, message, sensitiveData));
  }

  // ==================== CORE METHODS ====================

  private void publishAsync(String level, String eventName, String message, Throwable throwable, Map<String, Object> metadata) {
    String traceId = getTraceId();
    long id = loggerFactory.getSnowflake().next();
    long timestamp = DateTimeHelper.currentTimeMillis();

    boolean published = loggerFactory.getRingBuffer().publish(
        id, traceId, level, eventName, message,
        loggerFactory.getServiceName(), throwable, metadata, timestamp, clazz
    );

    // Fallback: if ring buffer is full, log directly (sync)
    if (!published) {
      logDirect(level, traceId, id, eventName, message, throwable, metadata, timestamp);
    }
  }

  private void logDirect(String level, String traceId, long id, String eventName, String message,
                         Throwable throwable, Map<String, Object> metadata, long timestamp) {
    logToConsole(level, traceId, message, throwable);
  }

  void logHandler(String level, String traceId, String eventName, String message,
                  Throwable throwable, Map<String, Object> metadata) {
    long id = loggerFactory.getSnowflake().next();
    long timestamp = DateTimeHelper.currentTimeMillis();

    loggerFactory.getRingBuffer().publish(
        id, traceId, level, eventName, message,
        loggerFactory.getServiceName(), throwable, metadata, timestamp, clazz
    );
  }

  private void logToConsole(String level, String traceId, String message, Throwable throwable) {
    if(!loggerFactory.isConsoleEnabled()) return;
    String fullMessage = "[" + traceId + "] [" + clazz.getSimpleName() + "] " + message;
    switch (level) {
      case LEVEL_ERROR -> {
        System.err.println("[ERROR] " + fullMessage);
        if (throwable != null) {
          StringWriter sw = new StringWriter();
          throwable.printStackTrace(new PrintWriter(sw));
          System.err.println(sw);
        }
      }
      case LEVEL_WARN -> System.out.println("[WARN] " + fullMessage);
      case LEVEL_DEBUG -> System.out.println("[DEBUG] " + fullMessage);
      default -> System.out.println("[INFO] " + fullMessage);
    }
  }

  private String getTraceId() {
    RequestContext context = RequestContextHolder.get();
    return context != null ? context.getTraceId() : UUID.randomUUID().toString();
  }

  private static Map<String, Object> copyMetadata(Map<String, Object> metadata) {
    return metadata == null || metadata.isEmpty() ? EMPTY_METADATA : Map.copyOf(metadata);
  }

  // ==================== SENSITIVE DATA HELPER ====================

  private record MutateHelperSensitive(Logger logger) {

    @MutateSensitiveData
    public void infoWithSensitiveData(String traceId, String eventName, String message, SensitiveData sensitiveData) {
      logger.logHandler(LEVEL_INFO, traceId, eventName, message, null, sensitiveData.getMutatedData());
    }

    @MutateSensitiveData
    public void warnWithSensitiveData(String traceId, String eventName, String message, SensitiveData sensitiveData) {
      logger.logHandler(LEVEL_WARN, traceId, eventName, message, null, sensitiveData.getMutatedData());
    }

    @MutateSensitiveData
    public void debugWithSensitiveData(String traceId, String eventName, String message, SensitiveData sensitiveData) {
      logger.logHandler(LEVEL_DEBUG, traceId, eventName, message, null, sensitiveData.getMutatedData());
    }

    @MutateSensitiveData
    public void errorWithSensitiveData(String traceId, String eventName, String message, Throwable throwable, SensitiveData sensitiveData) {
      logger.logHandler(LEVEL_ERROR, traceId, eventName, message, throwable, sensitiveData.getMutatedData());
    }
  }

}
