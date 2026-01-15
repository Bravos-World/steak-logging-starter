package com.bravos.steak.logging.starter.annotation;

import com.bravos.steak.logging.starter.transform.encrypt.EncryptHandler;
import com.bravos.steak.logging.starter.transform.encrypt.NoEncrypt;
import com.bravos.steak.logging.starter.transform.hash.HashHandler;
import com.bravos.steak.logging.starter.transform.hash.NoHash;
import com.bravos.steak.logging.starter.transform.mask.MaskHandler;
import com.bravos.steak.logging.starter.transform.mask.NoMask;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Designed by Bravos, not AI generated
/**
 * Annotation to mark a field as sensitive, specifying handlers for masking, hashing, and encryption.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

  // Whether to ignore this field during processing.
  boolean ignore() default true;

  // Class to handle masking of the sensitive data.
  Class<? extends MaskHandler> maskHandler() default NoMask.class;

  // Class to handle hashing of the sensitive data.
  Class<? extends HashHandler> hashHandler() default NoHash.class;

  // Class to handle encryption of the sensitive data.
  Class<? extends EncryptHandler> encryptHandler() default NoEncrypt.class;

}
