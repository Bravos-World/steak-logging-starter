package com.bravos.steak.logging.starter.transform.mask;

import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

public final class PhonePartialMask implements MaskHandler {

  @Override
  public String transform(@NonNull String value, TransformContext transformContext) {
    return value.startsWith("+") ? internationalMask(value) : nationalMask(value);
  }

  private String nationalMask(String value) {
    String digits = value.replaceAll("\\D", "");
    int n = digits.length();

    if (n <= 6) {
      return digits.substring(0, 2)
          + "*".repeat(Math.max(0, n - 4))
          + digits.substring(n - 2);
    }

    int head = 3;
    int tail = 3;

    return digits.substring(0, head)
        + "*".repeat(n - head - tail)
        + digits.substring(n - tail);
  }

  private String internationalMask(String value) {
    String digits = value.replaceAll("\\D", "");
    int n = digits.length();

    if (n <= 7) {
      return digits.substring(0, 3)
          + "*".repeat(Math.max(0, n - 5))
          + digits.substring(n - 2);
    }

    int head = 4;
    int tail = 3;

    return digits.substring(0, head)
        + "*".repeat(n - head - tail)
        + digits.substring(n - tail);
  }

}
