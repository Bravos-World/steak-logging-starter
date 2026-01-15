package com.bravos.steak.logging.starter.transform.mask;

import com.bravos.steak.logging.starter.model.TransformContext;

public class NoMask implements MaskHandler{
  @Override
  public String transform(String value, TransformContext transformContext) {
    return "";
  }
}
