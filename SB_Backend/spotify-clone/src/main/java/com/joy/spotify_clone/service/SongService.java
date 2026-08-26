package com.joy.spotify_clone.service;

import com.joy.spotify_clone.DTO.request.SongRequest;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import com.joy.spotify_clone.DTO.response.SongResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SongService {
    SongResponse addSong(SongRequest request, MultipartFile songFile, MultipartFile imageFile, String email);

    Object getAllSongs(Long userId, int page, int size, String search);

    SongResponse getSongById(Long id);

    SongResponse updateSong(Long songId, SongRequest request, MultipartFile songFile, MultipartFile imageFile, String email);

    MessageResponse deleteSong(Long songId, String email);
}
