package com.bravos.steak.logging.starter.configuration;

import com.bravos.steak.commonutils.shared.crypto.impl.AesEncryptionServiceImpl;
import com.bravos.steak.logging.starter.model.SecretProvider;
import com.bravos.steak.logging.starter.model.TransformContext;
import com.bravos.steak.logging.starter.transform.Transformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;

@AutoConfiguration
public class TransformerAutoConfiguration {

  @Value("${logging.hash.key}")
  private String hashKey;

  @Value("${logging.encrypt.key}")
  private String secretKeyString;

  @Bean
  @ConditionalOnMissingBean(Transformer.class)
  @ConditionalOnBean({ObjectMapper.class, TransformContext.class})
  public Transformer transformer(ObjectMapper objectMapper, TransformContext transformContext) {
    return new Transformer(objectMapper, transformContext);
  }

  @Bean
  @ConditionalOnMissingBean(TransformContext.class)
  public TransformContext transformContext() {
    return TransformContext.builder()
        .hashKey(hashKey)
        .secretProvider(new SecretProvider(convertSecretKey(secretKeyString)))
        .build();
  }

  private SecretKey convertSecretKey(String secretKeyString) {
    return new AesEncryptionServiceImpl().convertSecretKey(secretKeyString);
  }

}
