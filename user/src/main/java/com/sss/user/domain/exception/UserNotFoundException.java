package com.sss.user.domain.exception;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException() {
        super("User not found");
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
