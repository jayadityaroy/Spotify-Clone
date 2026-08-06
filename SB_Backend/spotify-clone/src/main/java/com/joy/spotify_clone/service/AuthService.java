package com.joy.spotify_clone.service;

import com.joy.spotify_clone.DTO.request.RegisterUserRequest;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import jakarta.validation.Valid;

public interface AuthService {
    MessageResponse registerUser(RegisterUserRequest request);
}
