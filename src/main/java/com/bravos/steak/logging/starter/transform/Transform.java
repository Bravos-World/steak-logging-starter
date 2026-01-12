package com.bravos.steak.logging.starter.transform;

import com.bravos.steak.logging.starter.model.TransformContext;

@FunctionalInterface
public interface Transform {

  String transform(String value, TransformContext transformContext);

}
