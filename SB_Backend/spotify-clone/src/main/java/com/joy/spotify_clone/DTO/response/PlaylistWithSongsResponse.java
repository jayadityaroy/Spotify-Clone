package com.joy.spotify_clone.DTO.response;

import com.joy.spotify_clone.entity.Playlist;
import com.joy.spotify_clone.entity.PlaylistSong;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistWithSongsResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long appUserId;
    private String appUserName;
    private Integer songCount;
    private List<SongInPlaylistResponse> songs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SongInPlaylistResponse {
        private Long songId;
        private String title;
        private String artist;
        private String songUrl;
        private String imageUrl;
        private Integer position;
        private LocalDateTime addedAt;
    }

    public static PlaylistWithSongsResponse fromEntity(Playlist playlist, List<PlaylistSong> playlistSongs, String baseUrl){
        PlaylistWithSongsResponse playlistWithSongsResponse = new PlaylistWithSongsResponse();
        playlistWithSongsResponse.setId(playlist.getId());
        playlistWithSongsResponse.setName(playlist.getName());
        playlistWithSongsResponse.setDescription(playlist.getDescription());
        playlistWithSongsResponse.setIsPublic(playlist.getIsPublic());
        playlistWithSongsResponse.setImageUrl(playlist.getImageUrl() != null ? baseUrl + playlist.getImageUrl() : null);
        playlistWithSongsResponse.setCreatedAt(playlist.getCreatedAt());
        playlistWithSongsResponse.setUpdatedAt(playlist.getUpdatedAt());
        playlistWithSongsResponse.setAppUserId(playlist.getAppUser().getId());
        playlistWithSongsResponse.setAppUserName(playlist.getAppUser().getName());
        playlistWithSongsResponse.setSongCount(playlistSongs.size());

        List<SongInPlaylistResponse> songs = playlistSongs.stream()
                .map(playlistSong -> {
                    SongInPlaylistResponse songInPlaylistResponse = new SongInPlaylistResponse();
                    songInPlaylistResponse.setSongId(playlistSong.getSong().getId());
                    songInPlaylistResponse.setTitle(playlistSong.getSong().getTitle());
                    songInPlaylistResponse.setArtist(playlistSong.getSong().getArtist());
                    songInPlaylistResponse.setSongUrl(playlistSong.getSong().getSongUrl() != null ? baseUrl + playlistSong.getSong().getSongUrl() : null);
                    songInPlaylistResponse.setImageUrl(playlistSong.getSong().getImageUrl() != null? baseUrl + playlistSong.getSong().getImageUrl() : null);
                    songInPlaylistResponse.setPosition(playlistSong.getPosition());
                    songInPlaylistResponse.setAddedAt(playlistSong.getAddedAt());
                    return songInPlaylistResponse;
                })
                .collect(Collectors.toList());
        playlistWithSongsResponse.setSongs(songs);

        return playlistWithSongsResponse;
    }
}
