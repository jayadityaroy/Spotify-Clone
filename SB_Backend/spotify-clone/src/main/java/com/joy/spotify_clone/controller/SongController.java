package com.joy.spotify_clone.controller;

import com.joy.spotify_clone.DTO.response.SongAiInsightsResponse;
import com.joy.spotify_clone.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/song")
public class SongController {

    @Autowired
    private SongService songService;

    @GetMapping("/getSongAiInsights/{songId}")
    public ResponseEntity<SongAiInsightsResponse> getSongAiInsights(@PathVariable Long songId) {
        SongAiInsightsResponse response = songService.getSongAiInsights(songId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
