package com.joy.spotify_clone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_song")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // "I belong to one of these"
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY) // "I belong to one of these"
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @Column(nullable = false)
    private Integer position; // Position of the song in the playlist
}
/*
Default Fetch Types for JPA Relationships:
@ManyToOne	EAGER
@OneToOne	EAGER
@OneToMany	LAZY
@ManyToMany	LAZY

Issue with changing FetchType of @ManyToOne from EAGER to LAZY:
It can lead to issues when you try to access the related entity outside of the context of an active session.
This is because LAZY loading means that the related entity is not loaded from the database until it is accessed for the first time (during the active session).
If the session is closed before you access the related entity, you will encounter a LazyInitializationException.

Fix:
Instead of returning the raw entity from the Service,
you convert it to a DTO (Data Transfer Object) — a plain simple object — inside the Service method, before it returns.
Because you're still inside the transaction at that point, you're allowed to touch the related entity safely.
*/
