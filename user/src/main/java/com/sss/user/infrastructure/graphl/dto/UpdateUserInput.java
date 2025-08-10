package com.sss.user.infrastructure.graphl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInput {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String bio;
    private String profilePictureUrl;
    private String dateOfBirth;
    private String gender;
    private String location;
    private String characterName;
    private String animeMangaSource;
    private String characterDescription;
    private String avatarUrl;
    private String coverImageUrl;
}

