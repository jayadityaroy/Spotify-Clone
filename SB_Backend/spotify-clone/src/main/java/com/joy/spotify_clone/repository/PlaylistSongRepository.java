package com.joy.spotify_clone.repository;

import com.joy.spotify_clone.entity.PlaylistSong;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    @Modifying
    @Transactional
    // Song_Id = go into song (existing property), then into id (property of Song), and delete by that id
        // deleteSongId = if PlaylistSong has a property songId
    void deleteBySong_Id(Long songId);
}
