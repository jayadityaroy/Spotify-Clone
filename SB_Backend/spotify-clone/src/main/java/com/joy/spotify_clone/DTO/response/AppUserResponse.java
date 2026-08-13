package com.joy.spotify_clone.DTO.response;

import com.joy.spotify_clone.entity.AppUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
// DTO class for sending user information in response
public class AppUserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppUserResponse fromEntity(AppUser appUser, String accessToken, String refreshToken) {
        AppUserResponse appUserResponse = new AppUserResponse();
        appUserResponse.setId(appUser.getId());
        appUserResponse.setName(appUser.getName());
        appUserResponse.setEmail(appUser.getEmail());
        appUserResponse.setRole(appUser.getRole());
        appUserResponse.setAccessToken(accessToken);
        appUserResponse.setRefreshToken(refreshToken);
        appUserResponse.setCreatedAt(appUser.getCreatedAt());
        appUserResponse.setUpdatedAt(appUser.getUpdatedAt());
        return appUserResponse;
    }
}
