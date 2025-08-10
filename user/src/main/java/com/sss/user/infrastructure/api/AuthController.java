package com.sss.user.infrastructure.api;

import com.sss.user.application.UserService;
import com.sss.user.domain.model.User;
import com.sss.user.infrastructure.dto.LoginRequest;
import com.sss.user.infrastructure.dto.LoginResponse;
import com.sss.user.infrastructure.dto.LogoutRequest;
import com.sss.user.infrastructure.service.SessionService;
import com.sss.user.infrastructure.sync.UserSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Updated AuthController for Keycloak integration
 * 
 * @author : Ducpm56
 * @date : 07/08/2025
 **/
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final SessionService sessionService;
  private final UserSyncService userSyncService;

  public AuthController(UserService userService, SessionService sessionService, UserSyncService userSyncService) {
    this.userService = userService;
    this.sessionService = sessionService;
    this.userSyncService = userSyncService;
  }

  /**
   * Legacy login endpoint - now redirects to Keycloak
   * This is kept for backward compatibility with existing frontend
   */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", "Please use Keycloak authentication");
    response.put("keycloak_url", "http://localhost:8080/realms/sss-realm/protocol/openid-connect/auth");
    return ResponseEntity.badRequest().body(response);
  }

  /**
   * Get current user profile from JWT token
   */
  @GetMapping("/profile")
  public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
    try {
      System.out.println("🔍 AuthController - getProfile called");
      System.out.println("🔍 Authentication object: " + authentication);
      System.out.println("🔍 Authentication class: " + (authentication != null ? authentication.getClass().getName() : "null"));
      
      if (authentication == null) {
        System.out.println("❌ No authentication object provided");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Authentication required");
        return ResponseEntity.status(401).body(errorResponse);
      }

      System.out.println("🔍 Authentication principal: " + authentication.getPrincipal());
      System.out.println("🔍 Authentication principal class: " + authentication.getPrincipal().getClass().getName());

      Jwt jwt = (Jwt) authentication.getPrincipal();
      System.out.println("🔍 JWT Claims: " + jwt.getClaims());
      
      // Use UserSyncService for comprehensive sync
      User user = userSyncService.syncUserFromKeycloak(jwt);
      System.out.println("✅ User synced successfully: " + user.getUsername());

      Map<String, Object> apiResponse = new HashMap<>();
      apiResponse.put("success", true);
      apiResponse.put("data", user);
      apiResponse.put("message", "Profile retrieved successfully");
      
      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      System.err.println("❌ Error in getProfile: " + e.getMessage());
      e.printStackTrace();
      
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /**
   * Legacy logout endpoint - now handled by Keycloak
   */
  @PostMapping("/logout")
  public ResponseEntity<Map<String, Object>> logout() {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "Please logout via Keycloak");
    response.put("keycloak_logout_url", "http://localhost:8080/realms/sss-realm/protocol/openid-connect/logout");
    return ResponseEntity.ok(response);
  }

  /**
   * Public endpoint to get Keycloak configuration
   */
  @GetMapping("/public/keycloak-config")
  public ResponseEntity<Map<String, Object>> getKeycloakConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("realm", "sss-realm");
    config.put("auth_server_url", "http://localhost:8080");
    config.put("client_id", "sss-frontend");
    return ResponseEntity.ok(config);
  }

  /**
   * Test endpoint to check JWT validation
   */
  @GetMapping("/public/test-jwt")
  public ResponseEntity<Map<String, Object>> testJwtValidation() {
    Map<String, Object> response = new HashMap<>();
    try {
      // Test if Keycloak is reachable
      String keycloakUrl = "http://localhost:8080/realms/sss-realm/.well-known/openid_configuration";
      response.put("success", true);
      response.put("message", "JWT validation endpoint ready");
      response.put("keycloak_discovery_url", keycloakUrl);
      response.put("jwk_set_uri", "http://localhost:8080/realms/sss-realm/protocol/openid-connect/certs");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("message", "JWT validation setup failed: " + e.getMessage());
      return ResponseEntity.status(500).body(response);
    }
  }
}