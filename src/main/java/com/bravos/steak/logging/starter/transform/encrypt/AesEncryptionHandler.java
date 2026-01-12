package com.bravos.steak.logging.starter.transform.encrypt;

import com.bravos.steak.commonutils.shared.crypto.AesEncryptionService;
import com.bravos.steak.commonutils.shared.crypto.impl.AesEncryptionServiceImpl;
import com.bravos.steak.logging.starter.model.TransformContext;
import lombok.NonNull;

import javax.crypto.SecretKey;

public class AesEncryptionHandler implements EncryptHandler {

  private static final AesEncryptionService aesEncryptionService = new AesEncryptionServiceImpl();

  @Override
  public String transform(String value, TransformContext transformContext) {
    SecretKey secretKey = transformContext.getSecretProvider().getSecretKey();
    return aesEncryptionService.encrypt(value, secretKey);
  }

  @Override
  public @NonNull String algorithm() {
    return "AES";
  }

}
