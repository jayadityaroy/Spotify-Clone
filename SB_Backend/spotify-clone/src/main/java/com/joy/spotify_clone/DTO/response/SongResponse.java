package com.joy.spotify_clone.DTO.response;

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
}
