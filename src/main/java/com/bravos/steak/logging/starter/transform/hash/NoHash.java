package com.bravos.steak.logging.starter.transform.hash;

import com.bravos.steak.logging.starter.model.TransformContext;

public class NoHash implements HashHandler{
  @Override
  public String algorithm() {
    return "";
  }

  @Override
  public String transform(String value, TransformContext transformContext) {
    return "";
  }
}
