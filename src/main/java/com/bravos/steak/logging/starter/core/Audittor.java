package com.bravos.steak.logging.starter.core;

import com.bravos.steak.commonutils.shared.helper.DateTimeHelper;
import com.bravos.steak.commonutils.shared.helper.Snowflake;
import com.bravos.steak.logging.starter.annotation.MutateSensitiveData;
import com.bravos.steak.logging.starter.model.AuditData;
import com.bravos.steak.logging.starter.model.AuditLog;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Builder
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Audittor {

  final String serviceName;

  final Snowflake snowflake;

  final KafkaTemplate<String, Object> kafkaTemplate;

  final ExecutorService executorService = new ThreadPoolExecutor(
      2,
      8,
      60L, TimeUnit.SECONDS,
      new ArrayBlockingQueue<>(5000),
      new ThreadPoolExecutor.AbortPolicy()
  );

  final ObjectMapper objectMapper;
  final String auditLogTopic = "audit.log";

  private final MutateSensitiveHelper mutateSensitiveHelper = new MutateSensitiveHelper(this);

  public void audit(AuditData auditData,
                    SensitiveData oldValue,
                    SensitiveData newValue,
                    Map<String, String> metadata) {
    executorService.submit(() -> mutateSensitiveHelper.audit(auditData, oldValue, newValue, metadata));
  }

  public void audit(AuditData auditData, SensitiveData sensitiveMetadata) {
    executorService.submit(() -> mutateSensitiveHelper.audit(auditData, sensitiveMetadata));
  }

  public void audit(AuditData auditData) {
    audit(auditData, null, null, null);
  }

  private record MutateSensitiveHelper(Audittor audittor) {

    @MutateSensitiveData
    public void audit(AuditData auditData,
                      SensitiveData oldValue,
                      SensitiveData newValue,
                      Map<String, String> metadata) {
      String metadataString = null;
      if (metadata != null && !metadata.isEmpty()) {
        metadataString = audittor.objectMapper.writeValueAsString(metadata);
      }

      String oldValueString = checkAndConvertSensitiveDataToString(oldValue);
      String newValueString = checkAndConvertSensitiveDataToString(newValue);

      AuditLog auditLog = AuditLog.builder()
          .id(audittor.snowflake.next())
          .userId(auditData.getUserId())
          .origin(auditData.getOrigin())
          .service(audittor.serviceName)
          .entityName(auditData.getEntityName())
          .entityId(auditData.getEntityId())
          .action(auditData.getAction())
          .oldValue(oldValueString)
          .newValue(newValueString)
          .metadata(metadataString)
          .message(auditData.getMessage())
          .success(auditData.getSuccess())
          .reason(auditData.getReason())
          .timestamp(DateTimeHelper.currentTimeMillis())
          .build();
      audittor.kafkaTemplate.send(audittor.auditLogTopic, auditLog);
    }

    @MutateSensitiveData
    public void audit(AuditData auditData, SensitiveData sensitiveMetadata) {
      String metadataString = checkAndConvertSensitiveDataToString(sensitiveMetadata);
      AuditLog auditLog = AuditLog.builder()
          .id(audittor.snowflake.next())
          .userId(auditData.getUserId())
          .origin(auditData.getOrigin())
          .service(audittor.serviceName)
          .entityName(auditData.getEntityName())
          .entityId(auditData.getEntityId())
          .action(auditData.getAction())
          .metadata(metadataString)
          .message(auditData.getMessage())
          .success(auditData.getSuccess())
          .reason(auditData.getReason())
          .timestamp(DateTimeHelper.currentTimeMillis())
          .build();
      audittor.kafkaTemplate.send(audittor.auditLogTopic, auditLog);
    }

    private String checkAndConvertSensitiveDataToString(SensitiveData sensitiveData) {
      if (sensitiveData != null) {
        if (!sensitiveData.isMutated()) {
          throw new IllegalStateException("Sensitive data has not been mutated. Use annotation @MutateSensitiveData on method.");
        }
        return audittor.objectMapper.writeValueAsString(sensitiveData.getMutatedData());
      }
      return null;
    }

  }

}
