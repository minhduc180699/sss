package com.sss.user.infrastructure.sync;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO cho việc update Keycloak User
 * 
 * @author : AI Assistant
 * @date : 08/01/2025
 */
@Data
@Builder
public class KeycloakUserUpdateDto {
    private String email;
    private String firstName;
    private String lastName;
    
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();
    
    public void addAttribute(String key, String value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
    }
}
