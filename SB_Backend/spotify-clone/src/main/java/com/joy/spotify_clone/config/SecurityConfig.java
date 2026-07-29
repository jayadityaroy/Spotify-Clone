package com.joy.spotify_clone.config;

import com.joy.spotify_clone.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**","/api/file/**", "/api/playlist/getAllPublicPlaylists", "/api/playlist/getPlaylistWithSongs/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:3000")); // Replace with your frontend URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE","PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

/*
Enables method-level security annotations (like @PreAuthorize),
In simple words: you can protect specific methods, not just URLs.
 */

/*
.cors(cors -> cors.configurationSource(corsConfigurationSource())):
Enables CORS using your custom corsConfigurationSource() bean.
Needed when frontend and backend run on different origins/ports.
 */
/*
.csrf(csrf -> csrf.disable()):
Disables CSRF protection.
Common for stateless JWT APIs (where you don’t use cookie-based session login forms).
 */
/*
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**","/api/file/**", "/api/playlist/getAllPublicPlaylists", "/api/playlist/getPlaylistWithSongs/**").permitAll()
    .anyRequest().authenticated()):

 This sets endpoint access rules:
requestMatchers(...).permitAll()
These paths are public; no login token required.
/api/auth/**
/api/file/**
/api/playlist/getAllPublicPlaylists
/api/playlist/getPlaylistWithSongs/**
.anyRequest().authenticated()
Every other endpoint requires authentication (JWT).
 */
/*
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)):
Tells Spring: do not create HTTP sessions.
Each request must carry auth data (JWT) itself.
This is exactly how stateless JWT auth should work.
 */
/*
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class):
Adds your custom JWT filter into Spring Security filter chain.
Places it before UsernamePasswordAuthenticationFilter.
So JWT is checked early, and authentication is set before protected endpoints are processed.
 */
/*
corsConfigurationSource(): Defines CORS rules
 */
/*
configuration.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:3000")):
Only requests from these frontend origins are allowed.
Angular default: 4200, React default: 3000.
 */
/*
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE","PATCH", "OPTIONS")):
Allows all request headers.
 */
/*
configuration.setExposedHeaders(Arrays.asList("Authorization")):
Allows frontend JS to read Authorization header from response.
Useful if backend sends token in header.
 */
/*
configuration.setAllowCredentials(true):
Allows credentials (cookies/auth) in cross-origin requests.
Must be used carefully with origin settings (you already specify concrete origins, which is good).
 */

/*
UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/**", configuration);
return source :

Applies this CORS config to all routes (/**).
Returns it so Spring can use it.
 */

/*
@Bean
public PasswordEncoder passwordEncoder():
Defines a PasswordEncoder bean available app-wide
 */
