package com.bravos.steak.logging.starter.configuration;

import com.bravos.steak.logging.starter.model.TransformContext;
import com.bravos.steak.logging.starter.transform.Transformer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
public class TransformerAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(Transformer.class)
  @ConditionalOnBean({ObjectMapper.class, TransformContext.class})
  public Transformer transformer(ObjectMapper objectMapper, TransformContext transformContext) {
    return new Transformer(objectMapper, transformContext);
  }

}
