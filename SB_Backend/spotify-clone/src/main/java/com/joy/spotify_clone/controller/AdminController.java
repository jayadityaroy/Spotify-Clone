package com.joy.spotify_clone.controller;

import com.joy.spotify_clone.DTO.request.SongRequest;
import com.joy.spotify_clone.DTO.response.SongResponse;
import com.joy.spotify_clone.service.SongService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@Validated // <-- turns on method-parameter validation for this whole class
public class AdminController {
    @Autowired
    private SongService songService;

    @PostMapping("/addSong")
    public ResponseEntity<SongResponse> addSong(
            @RequestParam("title") @NotBlank(message = "Title is required") @Size(max = 100, message = "Title must be at most 100 characters") String title,
            @RequestParam("artist") @NotBlank(message = "Artist is required") @Size(max = 100, message = "Artist must be at most 100 characters") String artist,
            @RequestParam("songFile")MultipartFile songFile,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication
            ){
        String email = authentication.getName();
        SongRequest request = new SongRequest(title, artist);
        SongResponse response = songService.addSong(request, songFile, imageFile, email);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/getAllSongs")
    public ResponseEntity<?> getAllSongs(
            @RequestParam(required = false) Long userId, // search parameter for filtering songs by userId
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) // search parameter for filtering songs by title or artist
    {
        return new ResponseEntity<>(songService.getAllSongs(userId, page, size, search), HttpStatus.OK);
    }
}
