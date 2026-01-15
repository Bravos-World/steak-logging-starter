package com.bravos.steak.logging.starter.annotation.aspect;

import com.bravos.steak.logging.starter.core.SensitiveData;
import com.bravos.steak.logging.starter.transform.Transformer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Map;

@Aspect
@RequiredArgsConstructor
public class MutateSensitveAspect {

  private final Transformer transformer;
  
  @Around("@annotation(com.bravos.steak.logging.starter.annotation.MutateSensitiveData)")
  public Object mutateSensitiveData(ProceedingJoinPoint joinPoint) {
    try {
      Object[] args = joinPoint.getArgs();
      for (Object arg : args) {
        transformArgument(arg);
      }
      return joinPoint.proceed(args);
    } catch (Throwable e) {
      throw new RuntimeException("Failed to mutate sensitive data", e);
    }
  }


  private void transformArgument(Object arg) throws IllegalAccessException {
    switch (arg) {
      case SensitiveData sensitiveData -> transformer.transform(sensitiveData);
      case SensitiveData[] sensitiveDataArray -> {
        for (SensitiveData sd : sensitiveDataArray) {
          transformer.transform(sd);
        }
      }
      case Iterable<?> iterable -> {
        for (Object item : iterable) {
          if (item instanceof SensitiveData sd) {
            transformer.transform(sd);
          }
        }
      }
      case Map<?, ?> map -> {
        for (Object value : map.values()) {
          if (value instanceof SensitiveData sd) {
            transformer.transform(sd);
          }
        }
      }
      default -> {
      }
    }
  }

}
