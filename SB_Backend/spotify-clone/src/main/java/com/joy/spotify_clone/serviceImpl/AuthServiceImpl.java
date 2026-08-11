package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.DTO.request.ForgotPasswordRequest;
import com.joy.spotify_clone.DTO.request.LoginUserRequest;
import com.joy.spotify_clone.DTO.request.RefreshTokenRequest;
import com.joy.spotify_clone.DTO.request.RegisterUserRequest;
import com.joy.spotify_clone.DTO.response.AppUserResponse;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import com.joy.spotify_clone.entity.AppUser;
import com.joy.spotify_clone.exception.EmailAlreadyExistException;
import com.joy.spotify_clone.exception.InvalidCredentialsException;
import com.joy.spotify_clone.exception.ResourceNotFoundException;
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
        appUser.setPassword(passwordEncoder.encode(tempPassword)); // hashed the password before saving
        String role = request.getRole();
        appUser.setRole(role != null ? role: "USER");
        appUserRepository.save(appUser);

        emailService.sendWelcomeEmail(request.getEmail(), request.getName(), tempPassword);
        return new MessageResponse("Welcome to Spotify Clone !! Your account has been created successfully. Please check your email for the temporary password.");
    }

    @Override
    public AppUserResponse loginUser(LoginUserRequest request) {
        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), appUser.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(appUser.getId(), appUser.getName(), appUser.getEmail(), appUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(appUser.getId(), appUser.getEmail());
        appUser.setRefreshToken(refreshToken);
        appUserRepository.save(appUser);
        return AppUserResponse.fromEntity(appUser, accessToken, refreshToken);
    }

    @Override
    public AppUserResponse refreshAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtUtil.extractEmail(refreshToken);
        AppUser appUser = appUserRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if(!jwtUtil.validateToken(refreshToken, email)){
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        String newAccessToken = jwtUtil.generateAccessToken(appUser.getId(), appUser.getName(), appUser.getEmail(), appUser.getRole());
        return AppUserResponse.fromEntity(appUser, newAccessToken, refreshToken);
    }

    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        AppUser appUser = appUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "+request.getEmail()));
        String tempPassword = generateTemporaryPassword();
        appUser.setPassword(passwordEncoder.encode(tempPassword));
        appUserRepository.save(appUser);
        emailService.sendCredentialsEmail(request.getEmail(), appUser.getName(), tempPassword);
        return new MessageResponse("Temporary password has been sent to your email.");
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
Working of registerUser():
1. Check if email already exists
2. if email doesn't exist in db: Generate temporary password, using generateTemporaryPassword()
3. Create a new AppUser
4. Save user to database
5. Send welcome email
6. Return success response
 */
/*
working of loginUser():
1. Find user by email
2. Verify password: The raw password from the login request is compared with the hashed password stored in the database.
3. Generate access token
4. Generate refresh token
5. Save refresh token in user record:
When access token expires, client sends refresh token to get a new one
Server checks stored refresh token and issues a new access token
6. return response with tokens
 */

/*
Working of refreshAccessToken():
1. Extract the refresh token from the request
2. Extract email from refresh token
3. Find user by refresh token
4. Validate refresh token: Check if the token is valid and not expired
5. Generate new access token
6. Return response with new access token and existing refresh token
 */

/*
Working of forgotPassword():
1. Find user by email
2. Generate temporary password using generateTemporaryPassword()
3. Update user's password in the database with the hashed temporary password
4. Send email to user with the temporary password
5. Return success response
 */

/*
Working of generateTemporaryPassword():
1. The method defines a string `chars` that contains a mix of uppercase letters, lowercase letters, numbers, and special characters.
This string serves as the pool of characters from which the temporary password will be generated.
2. A `SecureRandom` instance is created to generate cryptographically strong random numbers. This is important for security, as it ensures that the generated number is not predictable.
3. Using StringBuilder instead of String concatenation is more efficient, especially in a loop, because it reduces the number of intermediate String objects created.
4. A loop runs 10 times (for a password length of 10 characters). In each iteration, a random index is generated (0-chars.length()-1) using `random.nextInt(chars.length())`,
which selects a character from the `chars` string and the selected character is added in the 'password' StringBuilder.
 */


