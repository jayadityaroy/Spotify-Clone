package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.DTO.request.RegisterUserRequest;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import com.joy.spotify_clone.entity.AppUser;
import com.joy.spotify_clone.exception.EmailAlreadyExistException;
import com.joy.spotify_clone.repository.AppUserRepository;
import com.joy.spotify_clone.service.AuthService;
import com.joy.spotify_clone.service.EmailService;
import com.joy.spotify_clone.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Override
    public MessageResponse registerUser(RegisterUserRequest request) {
        if(appUserRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistException("Email already exists: " + request.getEmail());
        }
        String tempPassword = generateTemporaryPassword();
        AppUser appUser = new AppUser();
        appUser.setName(request.getName());
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(tempPassword));
        String role = request.getRole();
        appUser.setRole(role != null ? role: "USER");
        appUserRepository.save(appUser);

        emailService.sendWelcomeEmail(request.getEmail(), request.getName(), tempPassword);
        return new MessageResponse("Welcome to Spotify Clone !! Your account has been created successfully. Please check your email for the temporary password.");
    }

    private String generateTemporaryPassword() {
        String chars = "agaAHiMyNMAESIKnEECapPS!@#$@!#%snpoaidfa1234572453sapdidasmdfkaphnvaspdfjiasfdmaskdpifasdvmapsidgaifms";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for(int i = 0; i < 10; i++){
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}

/*
Working of generateTemporaryPassword():
1. The method defines a string `chars` that contains a mix of uppercase letters, lowercase letters, numbers, and special characters.
This string serves as the pool of characters from which the temporary password will be generated.
2. A `SecureRandom` instance is created to generate cryptographically strong random numbers. This is important for security, as it ensures that the generated number is not predictable.
3. Using StringBuilder instead of String concatenation is more efficient, especially in a loop, because it reduces the number of intermediate String objects created.
4. A loop runs 10 times (for a password length of 10 characters). In each iteration, a random index is generated (0-chars.length()-1) using `random.nextInt(chars.length())`,
which selects a character from the `chars` string and the selected character is added in the 'password' StringBuilder.
 */
