package com.bravos.steak.logging.starter.transform.mask;

import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

public final class EmailMask implements MaskHandler {

  @Override
  public String transform(@NonNull String value, TransformContext transformContext) {
    int atIndex = value.indexOf("@");
    String username = value.substring(0, atIndex);
    String domain = value.substring(atIndex);
    if (username.length() <= 1) {
      return "*@" + domain;
    } else if (username.length() == 2) {
      return username.charAt(0) + "*" + domain;
    } else {
      return username.charAt(0) + "****" + username.charAt(username.length() - 1) + domain;
    }
  }

}
