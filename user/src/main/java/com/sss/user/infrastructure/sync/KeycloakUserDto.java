package com.sss.user.infrastructure.sync;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * DTO cho Keycloak User data
 * 
 * @author : AI Assistant
 * @date : 08/01/2025
 */
@Data
@Builder
public class KeycloakUserDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean emailVerified;
    private Map<String, String> attributes;
    
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
    
    public String getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }
}
