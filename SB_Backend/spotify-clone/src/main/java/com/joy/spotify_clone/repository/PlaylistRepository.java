package com.joy.spotify_clone.repository;

import com.joy.spotify_clone.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    @Query("SELECT DISTINCT p FROM Playlist p JOIN PlaylistSong ps ON p.id = ps.playlist.id WHERE p.isPublic = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Playlist> findPublicPlaylistsWithSongsByNameOrDescription(String search, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Playlist p JOIN PlaylistSong ps ON p.id = ps.playlist.id WHERE p.isPublic = true")
    Page<Playlist> findPublicPlaylistsWithSongs(Pageable pageable);
}
// Working of findPublicPlaylistsWithSongsByNameOrDescription:
// 1. The query selects distinct playlists (p) from the Playlist entity.
// 2. It joins the PlaylistSong entity (ps) on the condition that the playlist ID in PlaylistSong matches the playlist ID in Playlist.
// 3. It filters the results to include only public playlists (p.isPublic = true).
// 4. It further filters the results to include playlists where the name or description contains the search term (case-insensitive).
// 5. The results are returned as a Page of Playlist objects, allowing for pagination.


