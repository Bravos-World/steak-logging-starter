package com.bravos.steak.logging.starter.transform.hash;

import com.bravos.steak.commonutils.shared.crypto.Hmac512Service;
import com.bravos.steak.commonutils.shared.crypto.impl.Hmac512ServiceImpl;
import com.bravos.steak.logging.starter.model.TransformContext;

public class Hmacsha512Handler implements HashHandler {

  private final Hmac512Service hmac512Service = new Hmac512ServiceImpl();

  @Override
  public String algorithm() {
    return "HMACSHA512";
  }

  @Override
  public String transform(String value, TransformContext transformContext) {
    return hmac512Service.signData(value, transformContext.getHashKey());
  }

}
