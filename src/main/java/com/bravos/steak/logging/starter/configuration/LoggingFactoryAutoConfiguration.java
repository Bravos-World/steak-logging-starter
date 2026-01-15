package com.bravos.steak.logging.starter.configuration;

import com.bravos.steak.commonutils.shared.helper.Snowflake;
import com.bravos.steak.logging.starter.annotation.aspect.MutateSensitveAspect;
import com.bravos.steak.logging.starter.core.Audittor;
import com.bravos.steak.logging.starter.core.LoggerFactory;
import com.bravos.steak.logging.starter.transform.Transformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
public class LoggingFactoryAutoConfiguration {

  @Value("${spring.application.name:unnamed-service}")
  private String serviceName;

  @Value("${logging.info:true}")
  private boolean infoEnabled;

  @Value("${logging.debug:false}")
  private boolean debugEnabled;

  @Value("${logging.error:true}")
  private boolean errorEnabled;

  @Value("${logging.warn:true}")
  private boolean warnEnabled;

  @Value("${logging.console:true}")
  private boolean consoleEnabled;

  @Bean
  @ConditionalOnMissingBean(LoggerFactory.class)
  @ConditionalOnBean({KafkaTemplate.class, Snowflake.class})
  public LoggerFactory loggerFactory(KafkaTemplate<String, Object> kafkaTemplate, Snowflake snowflake) {
    return LoggerFactory.builder()
        .kafkaTemplate(kafkaTemplate)
        .serviceName(serviceName)
        .snowflake(snowflake)
        .infoEnabled(infoEnabled)
        .debugEnabled(debugEnabled)
        .errorEnabled(errorEnabled)
        .warnEnabled(warnEnabled)
        .consoleEnabled(consoleEnabled)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(Audittor.class)
  @ConditionalOnBean({KafkaTemplate.class, Snowflake.class})
  public Audittor audittor(KafkaTemplate<String, Object> kafkaTemplate, Snowflake snowflake) {
    return Audittor.builder()
        .snowflake(snowflake)
        .kafkaTemplate(kafkaTemplate)
        .serviceName(serviceName)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(MutateSensitveAspect.class)
  @ConditionalOnBean(Transformer.class)
  public MutateSensitveAspect mutateSensitveAspect(Transformer transformer) {
    return new MutateSensitveAspect(transformer);
  }

}
