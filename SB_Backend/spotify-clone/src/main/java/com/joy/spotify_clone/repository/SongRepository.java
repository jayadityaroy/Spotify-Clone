package com.joy.spotify_clone.repository;

import com.joy.spotify_clone.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    Page<Song> findByAppUserIdAndTitleContainingIgnoreCaseOrAppUserIdAndArtistContainingIgnoreCase(Long userId, String title, Long userId1, String artist, Pageable pageable);

    Page<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artis, Pageable pageable);

    Page<Song> findByAppUserId(Long userId, Pageable pageable);
}
