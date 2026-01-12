package com.bravos.steak.logging.starter.transform.mask;

import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

public final class PhoneEndOnlyMask implements MaskHandler {

  @Override
  public String transform(@NonNull String value, TransformContext transformContext) {
    return value.startsWith("+") ? internationalMask(value) : nationalMask(value);
  }

  private String nationalMask(String value) {
    return "*".repeat(value.length() - 3) + value.substring(value.length() - 3);
  }

  private String internationalMask(String value) {
    value = value.replaceAll("-", " ");
    String countryCode = value.contains(" ") ? value.substring(0, value.indexOf(" ")) + " " : "";
    return countryCode + "*".repeat(value.length() - countryCode.length() - 3) + value.substring(value.length() - 3);
  }

}
