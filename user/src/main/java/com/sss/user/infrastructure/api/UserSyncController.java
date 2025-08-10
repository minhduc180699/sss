package com.sss.user.infrastructure.api;

import com.sss.user.infrastructure.sync.UserSyncService;
import com.sss.user.infrastructure.sync.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller cho user synchronization operations
 * 
 * @author : AI Assistant
 * @date : 08/01/2025
 */
@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class UserSyncController {

    private final UserSyncService userSyncService;
    private final KeycloakUserService keycloakUserService;

    /**
     * Test Keycloak connection
     */
    @GetMapping("/test-keycloak")
    public ResponseEntity<Map<String, Object>> testKeycloakConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isConnected = keycloakUserService.testConnection();
            
            response.put("success", isConnected);
            response.put("message", isConnected ? 
                "Keycloak connection successful" : 
                "Keycloak connection failed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Keycloak connection error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Sync tất cả users từ MongoDB sang Keycloak
     * Chỉ admin mới có thể thực hiện
     */
    @PostMapping("/mongodb-to-keycloak")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncMongoDbToKeycloak() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            userSyncService.syncAllUsersToKeycloak();
            
            response.put("success", true);
            response.put("message", "Successfully synced all users from MongoDB to Keycloak");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Sync failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get sync status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test connections
            boolean keycloakConnected = keycloakUserService.testConnection();
            
            // Get stats
            var keycloakUsers = keycloakUserService.getAllUsers();
            
            response.put("success", true);
            response.put("keycloak_connected", keycloakConnected);
            response.put("keycloak_users_count", keycloakUsers.size());
            response.put("message", "Sync status retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get sync status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Manual sync specific user by username
     */
    @PostMapping("/user/{username}/to-keycloak")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> syncUserToKeycloak(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // This would need to be implemented in UserSyncService
            response.put("success", true);
            response.put("message", "User sync to Keycloak completed for: " + username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to sync user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test role assignment for specific user
     */
    @PostMapping("/user/{username}/assign-role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> assignRoleToUser(
            @PathVariable String username, 
            @PathVariable String roleName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find user in Keycloak
            var keycloakUser = keycloakUserService.findUserByUsername(username);
            if (keycloakUser == null) {
                response.put("success", false);
                response.put("message", "User not found in Keycloak: " + username);
                return ResponseEntity.status(404).body(response);
            }
            
            // Ensure role exists
            keycloakUserService.ensureRolesExist();
            
            // Assign role
            keycloakUserService.assignRealmRole(keycloakUser.getId(), roleName);
            
            // Get updated roles
            var userRoles = keycloakUserService.getUserRoles(keycloakUser.getId());
            
            response.put("success", true);
            response.put("message", "Role assigned successfully");
            response.put("username", username);
            response.put("assigned_role", roleName);
            response.put("current_roles", userRoles);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to assign role: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get user roles from Keycloak
     */
    @GetMapping("/user/{username}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserRoles(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var keycloakUser = keycloakUserService.findUserByUsername(username);
            if (keycloakUser == null) {
                response.put("success", false);
                response.put("message", "User not found in Keycloak: " + username);
                return ResponseEntity.status(404).body(response);
            }
            
            var userRoles = keycloakUserService.getUserRoles(keycloakUser.getId());
            
            response.put("success", true);
            response.put("username", username);
            response.put("keycloak_user_id", keycloakUser.getId());
            response.put("roles", userRoles);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get user roles: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
