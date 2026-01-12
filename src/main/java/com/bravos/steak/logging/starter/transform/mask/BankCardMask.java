package com.bravos.steak.logging.starter.transform.mask;

import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

public final class BankCardMask implements MaskHandler {

  @Override
  public String transform(@NonNull String value, TransformContext transformContext) {
    return value
        .replaceAll(" ", "")
        .substring(0, 6)
        .concat("******")
        .concat(value.substring(value.length() - 4));
  }

}
