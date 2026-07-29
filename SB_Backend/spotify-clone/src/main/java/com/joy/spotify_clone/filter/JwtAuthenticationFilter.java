package com.joy.spotify_clone.filter;

import com.joy.spotify_clone.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            jwt = authorizationHeader.substring(7);
            try{
                email = jwtUtil.extractEmail(jwt);
            }
            catch (Exception e){
                logger.error("Jwt Token extraction failed: "+e.getMessage());
            }
        }
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            if(jwtUtil.validateToken(jwt, email) && jwtUtil.isAccessToken(jwt)){
                String role = jwtUtil.extractRole(jwt);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+role)));
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}

/*
extending OncePerRequestFilter, so it runs one time for each request
 */

/*
What each parameter means:
request = the incoming HTTP request
response = the HTTP response you may send back
filterChain = tells Spring to continue processing the request after this filter
 */

/*
final String authorizationHeader = request.getHeader("Authorization");
Reads the Authorization header from the request.
This header usually contains the JWT token.
Example: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */

/*
if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) == true
This means:
email was successfully extracted
and no user is already authenticated in the current security context
 */

/*
if(jwtUtil.validateToken(jwt, email) && jwtUtil.isAccessToken(jwt)) == true
This means:
token email matches the extracted email
token is not expired
token type is ACCESS
So only a valid access token can authenticate the request.
 */

/*
UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+role)));
An authenticated user object for Spring Security.
Parts inside:
    email = principal, meaning the user identity
    null = password is not needed here because JWT already authenticated the user
    Collections.singletonList(...) = a list containing one role
    new SimpleGrantedAuthority("ROLE_"+role) = converts role into Spring Security format
Example: If role is USER, then authority becomes:
    ROLE_USER
Why ROLE_ prefix?
    Spring Security expects roles in that format by default.
 */

/*
authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
Adds extra information from the current request to the authentication object.
This can include IP address, session info, etc.
 */

/*
SecurityContextHolder.getContext().setAuthentication(authenticationToken);
This tells Spring Security:
“This request is from an authenticated user”
After this line, controllers and security rules can use this authentication info.
 */

/*
filterChain.doFilter(request, response);
Passes the request to the next filter or controller.
Without this line, the request would stop here.
 */
