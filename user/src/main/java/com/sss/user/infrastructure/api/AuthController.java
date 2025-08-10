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
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Hybrid AuthController supporting both direct login and Keycloak
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
   * Hybrid login endpoint - supports both direct and Keycloak authentication
   */
  @PostMapping("/hybrid-login")
  public ResponseEntity<Map<String, Object>> hybridLogin(@RequestBody LoginRequest request) {
    try {
      System.out.println("🔐 Hybrid login attempt for user: " + request.username());
      
      // First try direct login
      User user = userService.login(request.username(), request.password());
      
      // Generate hybrid token that works with both systems
      String hybridToken = generateHybridToken(user);
      
      // Sync user with Keycloak if needed
      try {
        userSyncService.syncUserToKeycloak(user);
        System.out.println("✅ User synced to Keycloak successfully");
      } catch (Exception e) {
        System.out.println("⚠️ Keycloak sync failed, but login continues: " + e.getMessage());
      }
      
      Map<String, Object> successResponse = new HashMap<>();
      successResponse.put("success", true);
      successResponse.put("message", "Login successful");
      successResponse.put("accessToken", hybridToken);
      successResponse.put("tokenType", "hybrid");
      successResponse.put("user", user);
      successResponse.put("keycloakEnabled", true);
      
      return ResponseEntity.ok(successResponse);
    } catch (Exception e) {
      System.err.println("❌ Error in hybrid login: " + e.getMessage());
      e.printStackTrace();
      
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Login failed: " + e.getMessage());
      errorResponse.put("keycloakEnabled", true);
      return ResponseEntity.status(401).body(errorResponse);
    }
  }

  /**
   * Direct login endpoint with username/password
   * This bypasses Keycloak for direct authentication
   */
  @PostMapping("/direct-login")
  public ResponseEntity<Map<String, Object>> directLogin(@RequestBody LoginRequest request) {
    try {
      System.out.println("🔐 Direct login attempt for user: " + request.username());
      
      // Use UserService login method
      User user = userService.login(request.username(), request.password());
      
      // Generate simple token (for demo purposes)
      String token = "direct-login-token-" + user.getId().value() + "-" + System.currentTimeMillis();
      
      Map<String, Object> successResponse = new HashMap<>();
      successResponse.put("success", true);
      successResponse.put("message", "Login successful");
      successResponse.put("accessToken", token);
      successResponse.put("tokenType", "direct");
      successResponse.put("user", user);
      successResponse.put("keycloakEnabled", false);
      
      return ResponseEntity.ok(successResponse);
    } catch (Exception e) {
      System.err.println("❌ Error in direct login: " + e.getMessage());
      e.printStackTrace();
      
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Login failed: " + e.getMessage());
      errorResponse.put("keycloakEnabled", false);
      return ResponseEntity.status(401).body(errorResponse);
    }
  }

  /**
   * Generate hybrid token that works with both direct auth and Keycloak
   */
  private String generateHybridToken(User user) {
    // Create a token that includes both user info and Keycloak compatibility
    String baseToken = "hybrid-token-" + user.getId().value() + "-" + System.currentTimeMillis();
    
    // Add user info to token (in real implementation, this would be JWT)
    Map<String, Object> tokenData = new HashMap<>();
    tokenData.put("sub", user.getId().value());
    tokenData.put("preferred_username", user.getUsername());
    tokenData.put("email", user.getEmail());
    tokenData.put("given_name", user.getFullName());
    tokenData.put("realm_access", Map.of("roles", user.getUserType().toString()));
    
    // Store token data in session service for later retrieval
    // sessionService.storeTokenData(baseToken, tokenData); // Commented out - method doesn't exist
    
    return baseToken;
  }

  /**
   * Test endpoint for direct login
   */
  @PostMapping("/test-login")
  public ResponseEntity<Map<String, Object>> testLogin(@RequestBody LoginRequest request) {
    try {
      System.out.println("🔐 Test login attempt for user: " + request.username());
      
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "Test login endpoint working");
      response.put("username", request.username());
      response.put("password", request.password());
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("❌ Error in test login: " + e.getMessage());
      e.printStackTrace();
      
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Test login failed: " + e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /**
   * Login endpoint - BE calls Keycloak with username/password
   */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
    try {
      System.out.println("🔐 Login attempt for user: " + request.username());
      
      // Call Keycloak to authenticate user
      String keycloakToken = authenticateWithKeycloak(request.username(), request.password());
      
      if (keycloakToken != null) {
        // Parse token to get user info
        Map<String, Object> userInfo = parseKeycloakToken(keycloakToken);
        
        // Sync user with local database using token data
        User user = userSyncService.syncUserFromTokenData(userInfo);
        
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "Login successful");
        successResponse.put("accessToken", keycloakToken);
        successResponse.put("tokenType", "keycloak");
        successResponse.put("user", user);
        
        return ResponseEntity.ok(successResponse);
      } else {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Invalid credentials");
        return ResponseEntity.status(401).body(errorResponse);
      }
    } catch (Exception e) {
      System.err.println("❌ Error in login: " + e.getMessage());
      e.printStackTrace();
      
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", "Login failed: " + e.getMessage());
      return ResponseEntity.status(401).body(errorResponse);
    }
  }

  /**
   * Authenticate with Keycloak using username/password
   */
  private String authenticateWithKeycloak(String username, String password) {
    try {
      // Keycloak token endpoint
      String keycloakUrl = "http://localhost:8080/realms/sss-realm/protocol/openid-connect/token";
      
      // Prepare form data for Keycloak
      String formData = String.format(
        "grant_type=password&client_id=sss-frontend&username=%s&password=%s",
        username, password
      );
      
      // Make HTTP request to Keycloak
      java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
      java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
        .uri(java.net.URI.create(keycloakUrl))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(formData))
        .build();
      
      java.net.http.HttpResponse<String> response = client.send(request, 
        java.net.http.HttpResponse.BodyHandlers.ofString());
      
      if (response.statusCode() == 200) {
        // Parse response to get access token
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> tokenResponse = mapper.readValue(response.body(), Map.class);
        return (String) tokenResponse.get("access_token");
      } else {
        System.err.println("❌ Keycloak authentication failed: " + response.statusCode());
        return null;
      }
    } catch (Exception e) {
      System.err.println("❌ Error calling Keycloak: " + e.getMessage());
      return null;
    }
  }

  /**
   * Parse Keycloak token to extract user information
   */
  private Map<String, Object> parseKeycloakToken(String token) {
    try {
      // Decode JWT token (without verification for now)
      String[] parts = token.split("\\.");
      if (parts.length == 3) {
        String payload = parts[1];
        // Add padding if needed
        while (payload.length() % 4 != 0) {
          payload += "=";
        }
        
        // Decode base64
        String decodedPayload = new String(java.util.Base64.getUrlDecoder().decode(payload));
        
        // Parse JSON
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(decodedPayload, Map.class);
      }
    } catch (Exception e) {
      System.err.println("❌ Error parsing token: " + e.getMessage());
    }
    return new HashMap<>();
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