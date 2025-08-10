package com.sss.user.infrastructure.service;

import com.sss.user.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    
    // In production, use Redis or database to store sessions
    private final Map<String, User> tokenToUserMap = new ConcurrentHashMap<>();
    private final Map<String, String> userToTokenMap = new ConcurrentHashMap<>();
    
    public String createSession(User user) {
        // Remove existing session for this user if exists
        String existingToken = userToTokenMap.get(user.getId().value());
        if (existingToken != null) {
            tokenToUserMap.remove(existingToken);
        }
        
        // Create new session
        String token = java.util.UUID.randomUUID().toString();
        tokenToUserMap.put(token, user);
        userToTokenMap.put(user.getId().value(), token);
        
        return token;
    }
    
    public User getUserByToken(String token) {
        return tokenToUserMap.get(token);
    }
    
    public void removeSession(String token) {
        User user = tokenToUserMap.get(token);
        if (user != null) {
            userToTokenMap.remove(user.getId().value());
        }
        tokenToUserMap.remove(token);
    }
    
    public void removeSessionByUserId(String userId) {
        String token = userToTokenMap.get(userId);
        if (token != null) {
            tokenToUserMap.remove(token);
            userToTokenMap.remove(userId);
        }
    }
    
    public boolean isValidToken(String token) {
        return tokenToUserMap.containsKey(token);
    }
}

