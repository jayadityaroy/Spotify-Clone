package com.joy.spotify_clone.service;

import com.joy.spotify_clone.DTO.request.SongRequest;
import com.joy.spotify_clone.DTO.response.SongResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SongService {
    SongResponse addSong(SongRequest request, MultipartFile songFile, MultipartFile imageFile, String email);

    Object getAllSongs(Long userId, int page, int size, String search);
}
