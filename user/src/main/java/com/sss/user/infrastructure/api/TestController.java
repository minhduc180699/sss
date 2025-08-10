package com.sss.user.infrastructure.api;

import com.sss.user.application.UserService;
import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import com.sss.user.infrastructure.dto.UpdateUserRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Backend is running");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/connection")
    public Map<String, Object> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "connected");
        response.put("message", "Backend connection successful");
        return response;
    }

    @GetMapping("/auth-test")
    public Map<String, Object> testAuth(org.springframework.security.core.Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "No authentication found");
            return response;
        }
        
        // Get authorities
        java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = authentication.getAuthorities();
        
        response.put("success", true);
        response.put("username", authentication.getName());
        response.put("authorities", authorities.stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .collect(java.util.stream.Collectors.toList()));
        response.put("hasAdminRole", authorities.stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    @PostMapping("/login-test")
    public Map<String, Object> testLogin(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            User user = userService.login(username, password);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", Map.of(
                "id", user.getId().value(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "userType", user.getUserType().toString()
            ));
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/users")
    public Map<String, Object> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", users.size());
            response.put("users", users.stream().map(user -> Map.of(
                "id", user.getId().value(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "userType", user.getUserType().toString()
            )).toList());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get users: " + e.getMessage());
            response.put("error", e.toString());
            return response;
        }
    }

    @GetMapping("/user/{id}")
    public Map<String, Object> getUserById(@PathVariable String id) {
        try {
            var user = userService.getUserById(new UserId(id));
            if (user.isPresent()) {
                User userData = user.get();
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userData.getId().value());
                userMap.put("username", userData.getUsername());
                userMap.put("fullName", userData.getFullName());
                userMap.put("userType", userData.getUserType().toString());
                userMap.put("email", userData.getEmail());
                userMap.put("phoneNumber", userData.getPhoneNumber());
                userMap.put("address", userData.getAddress());
                userMap.put("bio", userData.getBio());
                userMap.put("profilePictureUrl", userData.getProfilePictureUrl());
                userMap.put("dateOfBirth", userData.getDateOfBirth());
                userMap.put("gender", userData.getGender());
                userMap.put("location", userData.getLocation());
                response.put("user", userMap);
                return response;
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found with ID: " + id);
                return response;
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get user: " + e.getMessage());
            response.put("error", e.toString());
            return response;
        }
    }

    @PutMapping("/update-user/{id}")
    public Map<String, Object> testUpdateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        try {
            System.out.println("🔍 Testing update user with ID: " + id);
            System.out.println("📝 Request data: " + request);
            
            UserId userId = new UserId(id);
            User updatedUser = userService.updateUserProfile(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User updated successfully");
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", updatedUser.getId().value());
            userMap.put("username", updatedUser.getUsername());
            userMap.put("fullName", updatedUser.getFullName());
            userMap.put("userType", updatedUser.getUserType().toString());
            userMap.put("email", updatedUser.getEmail());
            userMap.put("phoneNumber", updatedUser.getPhoneNumber());
            userMap.put("address", updatedUser.getAddress());
            userMap.put("bio", updatedUser.getBio());
            userMap.put("profilePictureUrl", updatedUser.getProfilePictureUrl());
            userMap.put("dateOfBirth", updatedUser.getDateOfBirth());
            userMap.put("gender", updatedUser.getGender());
            userMap.put("location", updatedUser.getLocation());
            response.put("user", userMap);
            return response;
        } catch (Exception e) {
            System.err.println("❌ Error updating user: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update user: " + e.getMessage());
            response.put("error", e.toString());
            return response;
        }
    }
}
