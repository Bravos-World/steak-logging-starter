package com.bravos.steak.logging.starter.transform.encrypt;

import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

public class NoEncrypt implements EncryptHandler {

  @Override
  public @NonNull String algorithm() {
    return "";
  }

  @Override
  public String transform(String value, TransformContext transformContext) {
    return "";
  }

}
