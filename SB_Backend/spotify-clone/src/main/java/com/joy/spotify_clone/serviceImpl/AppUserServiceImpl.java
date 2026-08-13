package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.DTO.request.AppUserRequest;
import com.joy.spotify_clone.DTO.response.AppUserResponse;
import com.joy.spotify_clone.DTO.response.PaginatedResponse;
import com.joy.spotify_clone.entity.AppUser;
import com.joy.spotify_clone.repository.AppUserRepository;
import com.joy.spotify_clone.service.AppUserService;
import jakarta.validation.Valid;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService {
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public AppUserResponse getUserProfile(String email) {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return AppUserResponse.fromEntity(appUser, null, null);
    }

    @Override
    public AppUserResponse updateUserProfile(AppUserRequest request, String email) {
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        if(request.getName() != null && !request.getName().trim().isEmpty()){
            appUser.setName(request.getName().trim());
        }
        if(request.getPassword() != null && !request.getPassword().trim().isEmpty()){
            if(request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()){
                throw new RuntimeException("Old password cannot be empty");
            }
            if(!passwordEncoder.matches(request.getOldPassword(), appUser.getPassword())){
                throw new RuntimeException("Old password is incorrect");
            }
            appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        AppUser updatedUser = appUserRepository.save(appUser);
        return AppUserResponse.fromEntity(updatedUser, null, null);
    }

    @Override
    public PaginatedResponse<AppUserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> userPage = appUserRepository.findAll(pageable);

        List<AppUserResponse> userResponses = userPage.getContent().stream()
                .map(user -> AppUserResponse.fromEntity(user, null, null))
                .collect(Collectors.toList());
        return new PaginatedResponse<>(userResponses, userPage.getNumber(), userPage.getSize(), userPage.getTotalElements(), userPage.getTotalPages(), userPage.isLast(), userPage.isFirst());
    }

    @Override
    public AppUserResponse updateUserRole(Long userId, String role, String email) {
        AppUser adminUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(!"ADMIN".equals(adminUser.getRole())){
            throw new RuntimeException("Only admin can update user roles");
        }

        AppUser userToUpdate = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        String standardizedRole = role.trim().toUpperCase();
        userToUpdate.setRole(standardizedRole);
        AppUser updatedUser = appUserRepository.save(userToUpdate);
        return AppUserResponse.fromEntity(updatedUser, null, null);
    }
}
/*
Working of updateUserProfile method:
1. Fetch the user by email from the database. If the user is not found, throw an exception.
2. Check if the request contains a new name (not null and not empty). If it does, update the user's name.
3. Check if the request contains a new password (not null and not empty). If it does, verify the old password and update the user's password.
4. Save the updated user to the database.
5. Return the updated user profile as an AppUserResponse.
 */
/*
Working of trim().isEmpty() method:
- The trim() method removes any leading and trailing whitespace from the string.
- The isEmpty() method checks if the string is empty (i.e., has a length of 0).
- By using trim().isEmpty(), we ensure that the string is not only empty but also does not consist solely of whitespace characters.
This is important for validating user input, as we want to prevent users from entering names or passwords that are just spaces, which would be considered invalid.
For example:
"".trim().isEmpty()        // true
"   ".trim().isEmpty()     // true
"abc".trim().isEmpty()     // false
" abc ".trim().isEmpty()   // false
 */

