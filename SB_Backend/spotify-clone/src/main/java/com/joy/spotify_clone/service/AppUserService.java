package com.joy.spotify_clone.service;

import com.joy.spotify_clone.DTO.request.AppUserRequest;
import com.joy.spotify_clone.DTO.response.AppUserResponse;
import com.joy.spotify_clone.DTO.response.PaginatedResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

public interface AppUserService {
    AppUserResponse getUserProfile(String email);

    AppUserResponse updateUserProfile(AppUserRequest appUserRequest, String email);

    PaginatedResponse<AppUserResponse> getAllUsers(int page, int size);

    AppUserResponse updateUserRole(Long userId, String role, String email);
}
