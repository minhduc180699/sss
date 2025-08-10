package com.sss.user.domain.model;

import java.util.UUID;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
public record UserId(String value) {
  public static UserId of(String id) {
    return new UserId(id);
  }
  
  public static UserId generate() {
    return new UserId(UUID.randomUUID().toString());
  }
}
