package com.joy.spotify_clone.service;

import com.joy.spotify_clone.DTO.request.AppUserRequest;
import com.joy.spotify_clone.DTO.response.AppUserResponse;
import jakarta.validation.Valid;

public interface AppUserService {
    AppUserResponse getUserProfile(String email);

    AppUserResponse updateUserProfile(AppUserRequest appUserRequest, String email);
}
