package com.bravos.steak.logging.starter.core;

/**
 * Zero-allocation SLF4J-style message formatter.
 * Supports {} placeholders and auto-detects Throwable at the end of arguments.
 */
public final class LogMessageFormatter {

  private static final String PLACEHOLDER = "{}";
  private static final int PLACEHOLDER_LENGTH = 2;

  private static final ThreadLocal<StringBuilder> STRING_BUILDER_HOLDER = ThreadLocal.withInitial(() -> new StringBuilder(256));

  private LogMessageFormatter() {
  }

  /**
   * Formats a message with SLF4J-style {} placeholders.
   *
   * @param pattern the message pattern
   * @param args    the arguments to substitute
   * @return formatted result containing message and optional throwable
   */
  public static FormattedResult format(String pattern, Object... args) {
    if (args == null || args.length == 0) {
      return new FormattedResult(pattern, null);
    }

    Throwable throwable = null;
    int argCount = args.length;

    // Auto-detect Throwable at end of args
    if (args[argCount - 1] instanceof Throwable t) {
      throwable = t;
      argCount--;
    }

    if (argCount == 0) {
      return new FormattedResult(pattern, throwable);
    }

    StringBuilder sb = STRING_BUILDER_HOLDER.get();
    sb.setLength(0);

    int argIndex = 0;
    int start = 0;
    int placeholderPos;

    while ((placeholderPos = pattern.indexOf(PLACEHOLDER, start)) != -1) {
      sb.append(pattern, start, placeholderPos);

      if (placeholderPos > 0 && pattern.charAt(placeholderPos - 1) == '\\') {
        // Escaped placeholder: \{} -> {}
        sb.setLength(sb.length() - 1);
        sb.append(PLACEHOLDER);
      } else if (argIndex < argCount) {
        appendArg(sb, args[argIndex++]);
      } else {
        sb.append(PLACEHOLDER);
      }

      start = placeholderPos + PLACEHOLDER_LENGTH;
    }

    sb.append(pattern, start, pattern.length());

    return new FormattedResult(sb.toString(), throwable);
  }

  private static void appendArg(StringBuilder sb, Object arg) {
    if (arg == null) {
      sb.append("null");
    } else if (arg instanceof String s) {
      sb.append(s);
    } else if (arg instanceof Number || arg instanceof Boolean || arg instanceof Character) {
      sb.append(arg);
    } else if (arg.getClass().isArray()) {
      appendArray(sb, arg);
    } else {
      sb.append(arg);
    }
  }

  private static void appendArray(StringBuilder sb, Object array) {
    sb.append('[');
    if (array instanceof Object[] objArray) {
      for (int i = 0; i < objArray.length; i++) {
        if (i > 0) sb.append(", ");
        appendArg(sb, objArray[i]);
      }
    } else if (array instanceof int[] intArray) {
      for (int i = 0; i < intArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(intArray[i]);
      }
    } else if (array instanceof long[] longArray) {
      for (int i = 0; i < longArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(longArray[i]);
      }
    } else if (array instanceof double[] doubleArray) {
      for (int i = 0; i < doubleArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(doubleArray[i]);
      }
    } else if (array instanceof boolean[] boolArray) {
      for (int i = 0; i < boolArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(boolArray[i]);
      }
    } else if (array instanceof byte[] byteArray) {
      for (int i = 0; i < byteArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(byteArray[i]);
      }
    } else if (array instanceof short[] shortArray) {
      for (int i = 0; i < shortArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(shortArray[i]);
      }
    } else if (array instanceof float[] floatArray) {
      for (int i = 0; i < floatArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(floatArray[i]);
      }
    } else if (array instanceof char[] charArray) {
      for (int i = 0; i < charArray.length; i++) {
        if (i > 0) sb.append(", ");
        sb.append(charArray[i]);
      }
    }
    sb.append(']');
  }

  /**
   * Result containing formatted message and optional throwable.
   */
  public record FormattedResult(String message, Throwable throwable) {
  }

}

