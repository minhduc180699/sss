package com.sss.user.infrastructure.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service để tương tác với Keycloak Admin API
 * 
 * @author : AI Assistant
 * @date : 08/01/2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserService {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm:sss-realm}")
    private String realm;
    
    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;
    
    @Value("${keycloak.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    private Keycloak keycloak;
    private RealmResource realmResource;

    @PostConstruct
    public void initKeycloak() {
        try {
            this.keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")  // Admin client always uses master realm
                .username(adminUsername)
                .password(adminPassword)
                .clientId(adminClientId)
                .build();
                
            this.realmResource = keycloak.realm(realm);
            log.info("✅ Keycloak Admin client initialized successfully");
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize Keycloak Admin client: {}", e.getMessage());
            throw new RuntimeException("Keycloak initialization failed", e);
        }
    }

    /**
     * Tìm user trong Keycloak by username
     */
    public KeycloakUserDto findUserByUsername(String username) {
        try {
            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.search(username, true);
            
            if (!users.isEmpty()) {
                UserRepresentation user = users.get(0);
                return convertToDto(user);
            }
            
            return null;
        } catch (Exception e) {
            log.error("❌ Error finding user by username {}: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * Tìm user trong Keycloak by email
     */
    public KeycloakUserDto findUserByEmail(String email) {
        try {
            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.searchByEmail(email, true);
            
            if (!users.isEmpty()) {
                UserRepresentation user = users.get(0);
                return convertToDto(user);
            }
            
            return null;
        } catch (Exception e) {
            log.error("❌ Error finding user by email {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Cập nhật user trong Keycloak
     */
    public void updateUser(String userId, KeycloakUserUpdateDto updateDto) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            
            // Update basic info
            if (updateDto.getEmail() != null) {
                user.setEmail(updateDto.getEmail());
            }
            if (updateDto.getFirstName() != null) {
                user.setFirstName(updateDto.getFirstName());
            }
            if (updateDto.getLastName() != null) {
                user.setLastName(updateDto.getLastName());
            }
            
            // Update custom attributes
            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            
            for (Map.Entry<String, String> entry : updateDto.getAttributes().entrySet()) {
                if (entry.getValue() != null) {
                    attributes.put(entry.getKey(), Collections.singletonList(entry.getValue()));
                }
            }
            user.setAttributes(attributes);
            
            // Perform update
            userResource.update(user);
            log.info("✅ Updated Keycloak user: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("❌ Error updating Keycloak user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update Keycloak user", e);
        }
    }

    /**
     * Tạo user mới trong Keycloak
     */
    public String createUser(KeycloakUserCreateDto createDto) {
        try {
            UsersResource usersResource = realmResource.users();
            
            UserRepresentation user = new UserRepresentation();
            user.setUsername(createDto.getUsername());
            user.setEmail(createDto.getEmail());
            user.setFirstName(createDto.getFirstName());
            user.setLastName(createDto.getLastName());
            user.setEnabled(true);
            user.setEmailVerified(true);
            
            // Set custom attributes
            if (!createDto.getAttributes().isEmpty()) {
                Map<String, List<String>> attributes = new HashMap<>();
                for (Map.Entry<String, String> entry : createDto.getAttributes().entrySet()) {
                    if (entry.getValue() != null) {
                        attributes.put(entry.getKey(), Collections.singletonList(entry.getValue()));
                    }
                }
                user.setAttributes(attributes);
            }
            
            // Create user
            var response = usersResource.create(user);
            
            if (response.getStatus() == 201) {
                // Extract user ID from location header
                String location = response.getLocation().toString();
                String userId = location.substring(location.lastIndexOf('/') + 1);
                
                log.info("✅ Created Keycloak user: {} with ID: {}", createDto.getUsername(), userId);
                return userId;
            } else {
                throw new RuntimeException("Failed to create user, status: " + response.getStatus());
            }
            
        } catch (Exception e) {
            log.error("❌ Error creating Keycloak user {}: {}", createDto.getUsername(), e.getMessage());
            throw new RuntimeException("Failed to create Keycloak user", e);
        }
    }

    /**
     * Xóa user khỏi Keycloak
     */
    public void deleteUser(String userId) {
        try {
            realmResource.users().delete(userId);
            log.info("✅ Deleted Keycloak user with ID: {}", userId);
        } catch (Exception e) {
            log.error("❌ Error deleting Keycloak user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to delete Keycloak user", e);
        }
    }

    /**
     * Lấy tất cả users từ Keycloak
     */
    public List<KeycloakUserDto> getAllUsers() {
        try {
            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> users = usersResource.list();
            
            return users.stream()
                .map(this::convertToDto)
                .toList();
                
        } catch (Exception e) {
            log.error("❌ Error getting all users: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Convert UserRepresentation to DTO
     */
    private KeycloakUserDto convertToDto(UserRepresentation user) {
        Map<String, String> attributes = new HashMap<>();
        if (user.getAttributes() != null) {
            user.getAttributes().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    attributes.put(key, values.get(0));
                }
            });
        }
        
        return KeycloakUserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .enabled(user.isEnabled())
            .emailVerified(user.isEmailVerified())
            .attributes(attributes)
            .build();
    }

    /**
     * Set password cho user trong Keycloak
     */
    public void setUserPassword(String userId, String password) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            
            // Create credential representation
            org.keycloak.representations.idm.CredentialRepresentation credential = 
                new org.keycloak.representations.idm.CredentialRepresentation();
            credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false); // User không cần đổi password lần đầu
            
            // Set password
            userResource.resetPassword(credential);
            log.info("✅ Set password for Keycloak user: {}", userId);
            
        } catch (Exception e) {
            log.error("❌ Error setting password for Keycloak user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to set password for Keycloak user", e);
        }
    }

    /**
     * Assign realm role to user
     */
    public void assignRealmRole(String userId, String roleName) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            
            // Get realm roles
            var realmRoles = realmResource.roles();
            var roleRepresentation = realmRoles.get(roleName).toRepresentation();
            
            // Assign role to user
            userResource.roles().realmLevel().add(java.util.List.of(roleRepresentation));
            
            log.info("✅ Assigned realm role '{}' to user: {}", roleName, userId);
            
        } catch (Exception e) {
            log.error("❌ Error assigning realm role '{}' to user {}: {}", roleName, userId, e.getMessage());
            throw new RuntimeException("Failed to assign realm role", e);
        }
    }

    /**
     * Remove realm role from user
     */
    public void removeRealmRole(String userId, String roleName) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            
            // Get realm roles
            var realmRoles = realmResource.roles();
            var roleRepresentation = realmRoles.get(roleName).toRepresentation();
            
            // Remove role from user
            userResource.roles().realmLevel().remove(java.util.List.of(roleRepresentation));
            
            log.info("✅ Removed realm role '{}' from user: {}", roleName, userId);
            
        } catch (Exception e) {
            log.error("❌ Error removing realm role '{}' from user {}: {}", roleName, userId, e.getMessage());
            throw new RuntimeException("Failed to remove realm role", e);
        }
    }

    /**
     * Get user roles
     */
    public java.util.List<String> getUserRoles(String userId) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            var roles = userResource.roles().realmLevel().listAll();
            
            return roles.stream()
                .map(org.keycloak.representations.idm.RoleRepresentation::getName)
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            log.error("❌ Error getting roles for user {}: {}", userId, e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Ensure required roles exist in realm
     */
    public void ensureRolesExist() {
        try {
            var realmRoles = realmResource.roles();
            
            // List of roles that should exist
            String[] requiredRoles = {"ADMIN", "ROLE_ADMIN", "USER", "CHARACTER"};
            
            for (String roleName : requiredRoles) {
                try {
                    // Try to get the role
                    realmRoles.get(roleName).toRepresentation();
                    log.info("✅ Role '{}' already exists", roleName);
                } catch (Exception e) {
                    // Role doesn't exist, create it
                    org.keycloak.representations.idm.RoleRepresentation role = 
                        new org.keycloak.representations.idm.RoleRepresentation();
                    role.setName(roleName);
                    role.setDescription("Auto-created role for SSS application");
                    
                    realmRoles.create(role);
                    log.info("✅ Created role: {}", roleName);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error ensuring roles exist: {}", e.getMessage());
        }
    }

    /**
     * Test connection to Keycloak
     */
    public boolean testConnection() {
        try {
            realmResource.toRepresentation();
            log.info("✅ Keycloak connection test successful");
            return true;
        } catch (Exception e) {
            log.error("❌ Keycloak connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
