package com.bravos.steak.logging.starter.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;

@RequiredArgsConstructor
public class SecretProvider {

  @Getter
  private final SecretKey secretKey;

}
