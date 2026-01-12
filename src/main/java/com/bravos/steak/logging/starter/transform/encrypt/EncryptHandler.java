package com.bravos.steak.logging.starter.transform.encrypt;

import com.bravos.steak.logging.starter.transform.Transform;
import lombok.NonNull;

public interface EncryptHandler extends Transform {

  @NonNull
  String algorithm();

}
