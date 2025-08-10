package com.sss.post.domain.exception;

/**
 * @author : Ducpm56
 * @date : 10/08/2025
 **/
public class PostNotFoundException extends RuntimeException {
  
  public PostNotFoundException(String message) {
    super(message);
  }
  
  public PostNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
