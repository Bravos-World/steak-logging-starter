package com.bravos.steak.logging.starter.transform.mask;

import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

public final class NationalIdentityMask implements MaskHandler {

  @Override
  public String transform(@NonNull String value, TransformContext transformContext) {
    return value.substring(0, 3)
        .concat("********")
        .concat(value.substring(value.length() - 3));
  }

}
