package com.joy.spotify_clone.controller;

import com.joy.spotify_clone.DTO.request.AppUserRequest;
import com.joy.spotify_clone.DTO.response.AppUserResponse;
import com.joy.spotify_clone.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appUser")
public class AppUserController {
    @Autowired
    private AppUserService appUserService;
    @GetMapping("/getUserProfile")
    public ResponseEntity<AppUserResponse> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        AppUserResponse appUserResponse = appUserService.getUserProfile(email);
        return new ResponseEntity<>(appUserResponse, HttpStatus.OK);
    }
    @PutMapping("/updateUserProfile")
    public ResponseEntity<AppUserResponse> updateUserProfile(@Valid @RequestBody AppUserRequest appUserRequest, Authentication authentication) {
        String email = authentication.getName();
        AppUserResponse updatedProfile = appUserService.updateUserProfile(appUserRequest, email);
        return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
    }
}
/*
Working of getUserProfile method:
1. The method is mapped to the GET request at the endpoint "/getUserProfile".
2. It takes an Authentication object as a parameter, which contains the details of the currently authenticated user.
3. The email of the authenticated user is retrieved using authentication.getName(). // email was set as the principal in JwtAuthenticationFilter while setting the authentication in the SecurityContextHolder.
// this will prevent users from accessing other users' profiles, as the email is extracted from the authenticated user's token.
4. The getUserProfile method of the appUserService is called with the extracted email to fetch the user's profile information.
5. The retrieved AppUserResponse object is then returned in the response.
 */
