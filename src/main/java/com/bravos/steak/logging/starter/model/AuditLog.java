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
public class AuditLog {

  Long id;

  Long userId;

  Origin origin;

  String service;

  String entityName;

  String entityId;

  String action;

  String oldValue;

  String newValue;

  String metadata;

  String message;

  Boolean success;

  String reason;

  Long timestamp;

}
