package com.joy.spotify_clone.service;

import com.joy.spotify_clone.DTO.request.PlaylistRequest;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import com.joy.spotify_clone.DTO.response.PaginatedResponse;
import com.joy.spotify_clone.DTO.response.PlaylistResponse;
import com.joy.spotify_clone.DTO.response.PlaylistWithSongsResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PlaylistService {
    PlaylistResponse createPlaylist(PlaylistRequest request, MultipartFile imageFile, String email);

    PlaylistResponse updatePlaylistPrivacy(Long id, Boolean isPublic, String email);

    MessageResponse addSongToPlaylist(Long playlistId, Long songId, String email);

    MessageResponse removeSongFromPlaylist(Long playlistId, Long songId, String email);

    MessageResponse reorderSongInPlaylist(Long playlistId, Long songId, Integer newPosition, String email);

    PaginatedResponse<PlaylistResponse> getAllPublicPlaylists(int page, int size, String search);

    PaginatedResponse<PlaylistResponse> getMyPlaylists(String email, int page, int size, String search);

    PlaylistWithSongsResponse getPlaylistWithSongs(Long playlistId, String email);
}
