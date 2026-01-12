package com.bravos.steak.logging.starter.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AuditData {

  Long userId;

  Origin origin;

  String service;

  String entityName;

  String entityId;

  String action;

  String message;

  Boolean success;

  String reason;

}
