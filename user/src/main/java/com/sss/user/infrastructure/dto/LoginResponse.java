package com.sss.user.infrastructure.dto;

import com.sss.user.domain.model.User;

/**
 * @author : Ducpm56
 * @date : 07/08/2025
 **/
public record LoginResponse(String token, User user) {}
