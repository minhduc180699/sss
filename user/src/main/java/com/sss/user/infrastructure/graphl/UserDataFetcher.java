package com.sss.user.infrastructure.graphl;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.sss.user.application.UserService;
import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import com.sss.user.infrastructure.dto.UpdateUserRequest;
import com.sss.user.infrastructure.graphl.dto.UserDto;
import com.sss.user.infrastructure.graphl.dto.UpdateUserInput;
import com.sss.user.infrastructure.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
@Slf4j
public class UserDataFetcher {

    private final UserService userService;
    private final UserSyncService userSyncService;

    @DgsQuery
    public UserDto user(@InputArgument String id) {
        Optional<User> user = userService.getUserById(new UserId(id));
        return user.map(this::mapToUserDto).orElse(null);
    }

    @DgsQuery
    public List<UserDto> users() {
        List<User> users = userService.getAllUsers();
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    @DgsQuery
    public UserDto currentUser() {
        try {
            User user = getCurrentAuthenticatedUser();
            log.info("🔍 Retrieved current user for GraphQL: {}", user.getUsername());
            return mapToUserDto(user);
            
        } catch (Exception e) {
            log.error("❌ Error getting current user in GraphQL: {}", e.getMessage());
            return null;
        }
    }

    @DgsMutation
    public UserDto updateUser(@InputArgument String id, @InputArgument UpdateUserInput input) {
        UpdateUserRequest request = mapToUpdateUserRequest(input);
        User updatedUser = userService.updateUserProfile(new UserId(id), request);
        return mapToUserDto(updatedUser);
    }

    @DgsMutation
    public UserDto updateCurrentUser(@InputArgument UpdateUserInput input) {
        try {
            // Get current authenticated user
            User currentUser = getCurrentAuthenticatedUser();
            log.info("🔍 Updating current user profile: {}", currentUser.getUsername());
            
            // Update user profile
            UpdateUserRequest request = mapToUpdateUserRequest(input);
            User updatedUser = userService.updateUserProfile(currentUser.getId(), request);
            
            log.info("✅ Successfully updated user profile for: {}", currentUser.getUsername());
            return mapToUserDto(updatedUser);
            
        } catch (Exception e) {
            log.error("❌ Error updating current user in GraphQL: {}", e.getMessage());
            throw new RuntimeException("Failed to update user profile: " + e.getMessage());
        }
    }

    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("Authentication required");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return userSyncService.syncUserFromKeycloak(jwt);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId().value())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .userType(user.getUserType())
                .characterName(user.getCharacterName())
                .animeMangaSource(user.getAnimeMangaSource())
                .characterDescription(user.getCharacterDescription())
                .avatarUrl(user.getAvatarUrl())
                .coverImageUrl(user.getCoverImageUrl())
                .characterStatus(user.getCharacterStatus())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .location(user.getLocation())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isLoggedIn(user.isLoggedIn())
                .isVerified(user.isVerified())
                .isActive(user.isActive())
                .build();
    }

    private UpdateUserRequest mapToUpdateUserRequest(UpdateUserInput input) {
        return new UpdateUserRequest(
                input.getFullName(),
                input.getEmail(),
                input.getPhoneNumber(),
                input.getAddress(),
                input.getBio(),
                input.getProfilePictureUrl(),
                input.getDateOfBirth(),
                input.getGender(),
                input.getLocation(),
                input.getCharacterName(),
                input.getAnimeMangaSource(),
                input.getCharacterDescription(),
                input.getAvatarUrl(),
                input.getCoverImageUrl()
        );
    }
}