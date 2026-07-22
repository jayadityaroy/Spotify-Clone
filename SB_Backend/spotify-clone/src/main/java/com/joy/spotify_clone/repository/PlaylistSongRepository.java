package com.joy.spotify_clone.repository;

import com.joy.spotify_clone.entity.PlaylistSong;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long>{
}
