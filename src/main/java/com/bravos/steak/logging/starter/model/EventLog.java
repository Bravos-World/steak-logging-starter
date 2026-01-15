package com.bravos.steak.logging.starter.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class EventLog {

  Long id;

  String traceId;

  String level;

  String eventName;

  String message;

  String service;

  String exceptionTrace;

  Map<String, Object> metadata;

  Long timestamp;

}
