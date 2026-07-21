package com.joy.spotify_clone.DTO.response;

import com.joy.spotify_clone.entity.Playlist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long appUserId;
    private String appUserName;

    public static PlaylistResponse fromEntity(Playlist playlist, String baseUrl) {
        PlaylistResponse playlistResponse = new PlaylistResponse();
        playlistResponse.setId(playlist.getId());
        playlistResponse.setName(playlist.getName());
        playlistResponse.setDescription(playlist.getDescription());
        playlistResponse.setIsPublic(playlist.getIsPublic());
        playlistResponse.setImageUrl(playlist.getImageUrl() != null ? baseUrl + playlist.getImageUrl(): null);
        playlistResponse.setCreatedAt(playlist.getCreatedAt());
        playlistResponse.setUpdatedAt(playlist.getUpdatedAt());
        playlistResponse.setAppUserId(playlist.getAppUser().getId());
        //                              ^^^^^^^^^^^^^^^^^^^   ^^^^^^
        //                              return locker object  opens the locker (query fires here)
        playlistResponse.setAppUserName(playlist.getAppUser().getName());
        //                                ^^^^^^^^^^^^^^^^^^^   ^^^^^^^^
        //                                same locker object    locker ALREADY open, just reads cached data

        return playlistResponse;
    }

}
