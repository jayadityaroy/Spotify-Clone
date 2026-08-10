package com.joy.spotify_clone.service;

import com.joy.spotify_clone.DTO.request.ForgotPasswordRequest;
import com.joy.spotify_clone.DTO.request.LoginUserRequest;
import com.joy.spotify_clone.DTO.request.RefreshTokenRequest;
import com.joy.spotify_clone.DTO.request.RegisterUserRequest;
import com.joy.spotify_clone.DTO.response.AppUserResponse;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import jakarta.validation.Valid;

public interface AuthService {
    MessageResponse registerUser(RegisterUserRequest request);

    AppUserResponse loginUser(LoginUserRequest request);

    AppUserResponse refreshAccessToken(RefreshTokenRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);
}
