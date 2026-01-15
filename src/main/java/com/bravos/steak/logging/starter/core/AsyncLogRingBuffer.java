package com.bravos.steak.logging.starter.core;

import com.bravos.steak.logging.starter.model.EventLog;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Lock-free ring buffer (LMAX Disruptor pattern) for async log event appending.
 * Provides high-throughput, low-latency logging with minimal garbage collection.
 */
public final class AsyncLogRingBuffer {

  private static final int DEFAULT_BUFFER_SIZE = 1024 * 16; // 16K entries, must be power of 2

  private final LogEvent[] buffer;
  private final int bufferSize;
  private final int indexMask;

  private final AtomicLong producerSequence = new AtomicLong(-1);
  private final AtomicLong consumerSequence = new AtomicLong(-1);

  private final LogEventConsumer consumer;
  private final Thread consumerThread;
  private volatile boolean running = true;

  public AsyncLogRingBuffer(LogEventConsumer consumer) {
    this(DEFAULT_BUFFER_SIZE, consumer);
  }

  public AsyncLogRingBuffer(int bufferSize, LogEventConsumer consumer) {
    if ((bufferSize & (bufferSize - 1)) != 0) {
      throw new IllegalArgumentException("Buffer size must be a power of 2");
    }
    this.bufferSize = bufferSize;
    this.indexMask = bufferSize - 1;
    this.buffer = new LogEvent[bufferSize];
    this.consumer = consumer;

    // Pre-allocate all LogEvent objects (zero-allocation during runtime)
    for (int i = 0; i < bufferSize; i++) {
      buffer[i] = new LogEvent();
    }

    // Start consumer thread
    this.consumerThread = Thread.ofVirtual()
        .name("async-log-consumer")
        .start(this::consumeLoop);
  }

  /**
   * Publishes a log event to the ring buffer.
   * Lock-free operation using CAS.
   *
   * @return true if published successfully, false if buffer is full
   */
  public boolean publish(
      long id,
      String traceId,
      String level,
      String eventName,
      String message,
      String service,
      Throwable throwable,
      Map<String, Object> metadata,
      long timestamp,
      Class<?> sourceClass
  ) {
    long currentProducer;
    long nextSequence;

    do {
      currentProducer = producerSequence.get();
      nextSequence = currentProducer + 1;

      // Check if buffer is full
      long wrapPoint = nextSequence - bufferSize;
      if (wrapPoint > consumerSequence.get()) {
        // Buffer full - apply backpressure strategy (drop)
        return false;
      }
    } while (!producerSequence.compareAndSet(currentProducer, nextSequence));

    // Claim the slot and populate
    int index = (int) (nextSequence & indexMask);
    LogEvent event = buffer[index];
    event.set(id, traceId, level, eventName, message, service, throwable, metadata, timestamp, sourceClass);
    event.markPublished();

    return true;
  }

  private void consumeLoop() {
    long nextSequence = 0;
    while (running) {
      long availableSequence = producerSequence.get();

      if (nextSequence <= availableSequence) {
        // Process available events
        while (nextSequence <= availableSequence) {
          int index = (int) (nextSequence & indexMask);
          LogEvent event = buffer[index];

          // Wait for the event to be fully published
          while (!event.isPublished()) {
            Thread.onSpinWait();
          }

          try {
            consumer.onEvent(event);
          } catch (Exception e) {
            // Log consumer error but continue processing
            System.err.println("Error consuming log event: " + e.getMessage());
          }

          event.reset();
          nextSequence++;
        }
        consumerSequence.set(nextSequence - 1);
      } else {
        // No events available - park briefly
        LockSupport.parkNanos(100_000L); // 100 microseconds
      }
    }

    // Drain remaining events on shutdown
    drainRemaining(nextSequence);
  }

  private void drainRemaining(long nextSequence) {
    long availableSequence = producerSequence.get();
    while (nextSequence <= availableSequence) {
      int index = (int) (nextSequence & indexMask);
      LogEvent event = buffer[index];
      if (event.isPublished()) {
        try {
          consumer.onEvent(event);
        } catch (Exception e) {
          System.err.println("Error draining log event: " + e.getMessage());
        }
        event.reset();
      }
      nextSequence++;
    }
  }

  public void shutdown() {
    running = false;
    LockSupport.unpark(consumerThread);
    try {
      consumerThread.join(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Pre-allocated log event for zero-allocation logging.
   */
  public static final class LogEvent {
    // Getters for consumer access
    @Getter
    private long id;
    @Getter
    private String traceId;
    @Getter
    private String level;
    @Getter
    private String eventName;
    @Getter
    private String message;
    @Getter
    private String service;
    @Getter
    private Throwable throwable;
    @Getter
    private Map<String, Object> metadata;
    @Getter
    private long timestamp;
    @Getter
    private Class<?> sourceClass;
    private volatile boolean published;

    void set(
        long id,
        String traceId,
        String level,
        String eventName,
        String message,
        String service,
        Throwable throwable,
        Map<String, Object> metadata,
        long timestamp,
        Class<?> sourceClass
    ) {
      this.id = id;
      this.traceId = traceId;
      this.level = level;
      this.eventName = eventName;
      this.message = message;
      this.service = service;
      this.throwable = throwable;
      this.metadata = metadata;
      this.timestamp = timestamp;
      this.sourceClass = sourceClass;
    }

    void markPublished() {
      this.published = true;
    }

    boolean isPublished() {
      return published;
    }

    void reset() {
      this.published = false;
      this.throwable = null;
      this.metadata = null;
    }

    public EventLog toEventLog() {
      String messageWithClass = "[" + traceId + "] [" + sourceClass.getSimpleName() + "] " + message;
      return EventLog.builder()
          .id(id)
          .traceId(traceId)
          .level(level)
          .eventName(eventName)
          .message(messageWithClass)
          .service(service)
          .exceptionTrace(throwable != null ? throwable.toString() : null)
          .metadata(metadata)
          .timestamp(timestamp)
          .build();
    }

  }

  /**
   * Consumer interface for processing log events.
   */
  @FunctionalInterface
  public interface LogEventConsumer {
    void onEvent(LogEvent event);
  }

}

