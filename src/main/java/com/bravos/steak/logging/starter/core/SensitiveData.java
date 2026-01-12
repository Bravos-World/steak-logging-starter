package com.bravos.steak.logging.starter.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.Map;

public abstract class SensitiveData {

  @Getter
  @JsonIgnore
  private Map<String, Object> mutatedData;

  @Getter
  @JsonIgnore
  private boolean mutated = false;

  public final void markAsMutated() {
    this.mutated = true;
  }

  public final void setMutatedData(Map<String, Object> mutatedData) {
    if(mutated) {
      throw new IllegalStateException("Mutated data has already been set and cannot be modified again.");
    }
    this.mutatedData = mutatedData;
  }

}
