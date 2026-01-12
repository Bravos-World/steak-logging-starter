package com.bravos.steak.logging.starter.transform.mask;

import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

public final class PassportMask implements MaskHandler {

  @Override
  public String transform(@NonNull String value, TransformContext transformContext) {
    int midStart = (value.length() - 4) / 2;
    return value.substring(0, midStart)
        .concat("****")
        .concat(value.substring(midStart + 4));
  }

}
