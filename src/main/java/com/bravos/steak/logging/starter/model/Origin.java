package com.bravos.steak.logging.starter.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class Origin implements Serializable {

  String userAgent;

  String ipAddress;

  String location;

}
