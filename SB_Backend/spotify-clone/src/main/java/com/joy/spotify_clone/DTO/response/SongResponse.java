package com.joy.spotify_clone.DTO.response;

import com.joy.spotify_clone.entity.Song;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {
    private Long id;
    private String title;
    private String artist;
    private String songUrl;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Long appUserId;
    private String appUserName;

    public static SongResponse fromEntity(Song song, String baseUrl) {
        SongResponse songResponse = new SongResponse();
        songResponse.setId(song.getId());
        songResponse.setTitle(song.getTitle());
        songResponse.setArtist(song.getArtist());
        songResponse.setSongUrl(song.getSongUrl() != null? baseUrl + song.getSongUrl() : null);
        songResponse.setImageUrl(song.getImageUrl() != null? baseUrl + song.getImageUrl() : null);
        songResponse.setCreatedAt(song.getCreatedAt());
        songResponse.setAppUserId(song.getAppUser().getId());
        songResponse.setAppUserName(song.getAppUser().getName());

        return songResponse;
    }
}
