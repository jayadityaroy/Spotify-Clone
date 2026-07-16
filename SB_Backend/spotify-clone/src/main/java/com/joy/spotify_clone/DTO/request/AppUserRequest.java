package com.joy.spotify_clone.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRequest {
    @Size(min=2,max=50, message = "Name must be between 2 and 50 characters")
    private String name;
    @Email(message = "Email should be valid")
    private String email;
    @Size(min=6, message = "Password must be at least 6 characters long")
    private String password;
    private String oldPassword;

    //Restricts role to only USER or ADMIN, and gives a custom error message if it doesn’t match.
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be either USER or ADMIN")
    private String role;

    private String refreshToken;
}
