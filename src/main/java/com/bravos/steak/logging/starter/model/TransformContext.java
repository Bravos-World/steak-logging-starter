package com.bravos.steak.logging.starter.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class TransformContext {

  SecretProvider secretProvider;

  String hashKey;

}
