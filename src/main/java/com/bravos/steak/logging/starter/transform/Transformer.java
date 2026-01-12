package com.bravos.steak.logging.starter.transform;

import com.bravos.steak.logging.starter.annotation.Sensitive;
import com.bravos.steak.logging.starter.core.SensitiveData;
import com.bravos.steak.logging.starter.model.TransformContext;
import com.bravos.steak.logging.starter.transform.encrypt.EncryptHandler;
import com.bravos.steak.logging.starter.transform.hash.HashHandler;
import com.bravos.steak.logging.starter.transform.mask.MaskHandler;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class Transformer {

  private static final String MASKED = "masked";

  private static final String HASHED = "hashed";
  private static final String HASH_ALG = "hash-alg";

  private static final String ENCRYPTED = "encrypted";
  private static final String ENC_ALG = "enc-alg";

  private static final Map<Class<?>, Object> handlerInstanceCache = new ConcurrentHashMap<>();

  private final ObjectMapper objectMapper;

  private final TransformContext transformContext;

  public void transform(final SensitiveData sensitiveData) throws IllegalAccessException {
    if (!sensitiveData.isMutated()) {
      Map<String, Object> mutatedData = new HashMap<>();
      for (Field field : sensitiveData.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        Object value = field.get(sensitiveData);

        if (value == null) continue;
        if (!field.isAnnotationPresent(Sensitive.class)) {
          mutatedData.put(field.getName(), value);
          continue;
        }

        Sensitive sensitive = field.getAnnotationsByType(Sensitive.class)[0];

        if (sensitive.ignore()) continue;

        Map<String, String> sensitiveTransformedData = new HashMap<>(5);

        String valueStr = objectMapper.writeValueAsString(value);

        if (sensitive.maskHandler() != null) {
          MaskHandler maskHandler = getMaskHandlerInstance(sensitive.maskHandler());
          sensitiveTransformedData.put(MASKED, maskHandler.transform(valueStr, transformContext));
        }

        if (sensitive.hashHandler() != null) {
          HashHandler hashHandler = getHashHandler(sensitive.hashHandler());
          sensitiveTransformedData.put(HASH_ALG, hashHandler.algorithm());
          sensitiveTransformedData.put(HASHED, hashHandler.transform(valueStr, transformContext));
        }

        if (sensitive.encryptHandler() != null) {
          EncryptHandler encryptHandler = getEncryptHandler(sensitive.encryptHandler());
          sensitiveTransformedData.put(ENC_ALG, encryptHandler.algorithm());
          sensitiveTransformedData.put(ENCRYPTED, encryptHandler.transform(valueStr, transformContext));
        }

        mutatedData.put(field.getName(), sensitiveTransformedData);
      }
      sensitiveData.setMutatedData(Collections.unmodifiableMap(mutatedData));
      sensitiveData.markAsMutated();
    }
  }

  private MaskHandler getMaskHandlerInstance(Class<? extends MaskHandler> maskHandlerClass) {
    return (MaskHandler) handlerInstanceCache.computeIfAbsent(maskHandlerClass, cls -> {
      try {
        return cls.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private HashHandler getHashHandler(Class<? extends HashHandler> hashHandlerClass) {
    return (HashHandler) handlerInstanceCache.computeIfAbsent(hashHandlerClass, cls -> {
      try {
        return cls.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private EncryptHandler getEncryptHandler(Class<? extends EncryptHandler> encryptHandlerClass) {
    return (EncryptHandler) handlerInstanceCache.computeIfAbsent(encryptHandlerClass, cls -> {
      try {
        return cls.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    });
  }

}
