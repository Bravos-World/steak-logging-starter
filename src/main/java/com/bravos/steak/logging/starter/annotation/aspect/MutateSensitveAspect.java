package com.bravos.steak.logging.starter.annotation.aspect;

import com.bravos.steak.logging.starter.core.SensitiveData;
import com.bravos.steak.logging.starter.transform.Transformer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

  @Around("@annotation(com.bravos.steak.logging.starter.annotation.MutateSensitiveDataAsync)")
  public Publisher<?> mutateSensitiveDataAsync(ProceedingJoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Class<?> returnType = methodSignature.getReturnType();

    if(!Publisher.class.isAssignableFrom(returnType)) {
      throw new IllegalStateException("@MutateSensitiveDataAsync can only be applied to methods returning Mono or Flux");
    }

    Mono<Object[]> argsTransformMono = Mono.fromCallable(() -> {
      Object[] args = joinPoint.getArgs();
      for (Object arg : args) {
        transformArgument(arg);
      }
      return args;
    }).subscribeOn(Schedulers.parallel());

    if (Mono.class.isAssignableFrom(returnType)) {
      return argsTransformMono.flatMap(mutatedArgs -> {
        try {
          return (Mono<?>) joinPoint.proceed(mutatedArgs);
        } catch (Throwable e) {
          return Mono.error(e);
        }
      });
    }

    if (Flux.class.isAssignableFrom(returnType)) {
      return argsTransformMono.flatMapMany(mutatedArgs -> {
        try {
          return (Flux<?>) joinPoint.proceed(mutatedArgs);
        } catch (Throwable e) {
          return Flux.error(e);
        }
      });
    }

    return argsTransformMono.flatMap(mutatedArgs -> {
      try {
        return Mono.justOrEmpty(joinPoint.proceed(mutatedArgs));
      } catch (Throwable e) {
        return Mono.error(e);
      }
    });
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
