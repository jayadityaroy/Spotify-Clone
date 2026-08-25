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
        songResponse.setSongUrl(song.getSongUrl() != null? baseUrl + song.getSongUrl() : null); // Eg: If songUrl is "api/file/song/s1.mp3" and baseUrl is "http://localhost:8080", the final URL will be "http://localhost:8080/api/file/song/s1.mp3"
        songResponse.setImageUrl(song.getImageUrl() != null? baseUrl + song.getImageUrl() : null); // Eg: If imageUrl is "api/file/image/s1.jpg" and baseUrl is "http://localhost:8080", the final URL will be "http://localhost:8080/api/file/image/s1.jpg"
        songResponse.setCreatedAt(song.getCreatedAt());
        songResponse.setAppUserId(song.getAppUser().getId());
        songResponse.setAppUserName(song.getAppUser().getName());

        return songResponse;
    }
}
/*
Use of songUrl and imageUrl:
The songUrl and imageUrl fields in the SongResponse class are used to provide the complete URLs for accessing the song and its associated image
using the endpoints defined in the FileController.
When a song is added, the song file and image file are saved on the server, and their respective URLs are generated.
The baseUrl is prepended to these URLs to create complete URLs that can be accessed by clients.
 */
