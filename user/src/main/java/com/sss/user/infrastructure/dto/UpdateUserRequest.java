package com.sss.user.infrastructure.dto;

/**
 * @author : Ducpm56
 * @date : 08/08/2025
 **/
public record UpdateUserRequest(
    String fullName,
    String email,
    String phoneNumber,
    String address,
    String bio,
    String profilePictureUrl,
    String dateOfBirth,
    String gender,
    String location,
    // Thông tin cho nhân vật
    String characterName,
    String animeMangaSource,
    String characterDescription,
    String avatarUrl,
    String coverImageUrl
) {}
