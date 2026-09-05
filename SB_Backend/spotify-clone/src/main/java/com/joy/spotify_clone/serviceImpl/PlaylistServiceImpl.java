package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.DTO.request.PlaylistRequest;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import com.joy.spotify_clone.DTO.response.PlaylistResponse;
import com.joy.spotify_clone.entity.AppUser;
import com.joy.spotify_clone.entity.Playlist;
import com.joy.spotify_clone.entity.PlaylistSong;
import com.joy.spotify_clone.entity.Song;
import com.joy.spotify_clone.repository.AppUserRepository;
import com.joy.spotify_clone.repository.PlaylistRepository;
import com.joy.spotify_clone.repository.PlaylistSongRepository;
import com.joy.spotify_clone.repository.SongRepository;
import com.joy.spotify_clone.service.PlaylistService;
import com.joy.spotify_clone.util.FileHandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class PlaylistServiceImpl implements PlaylistService {
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PlaylistSongRepository playlistSongRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private FileHandlerUtil fileHandlerUtil;
    @Value("${app.base.url}")
    private String baseUrl;

    @Override
    public PlaylistResponse createPlaylist(PlaylistRequest request, MultipartFile imageFile, String email) {
        AppUser appUser = getUserByEmail(email);
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setIsPublic(request.getIsPublic());
        playlist.setAppUser(appUser);
        if(imageFile != null && !imageFile.isEmpty()) {
            String uniqueId = UUID.randomUUID().toString();
            String imageExtension = fileHandlerUtil.getFileExtension(imageFile.getOriginalFilename());
            String imageFileName = uniqueId + imageExtension;
            fileHandlerUtil.saveImageFileWithName(imageFile, imageFileName);
            playlist.setImageUrl("/api/file/image/" + imageFileName);
        }
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return PlaylistResponse.fromEntity(savedPlaylist, baseUrl);
    }

    @Override
    public PlaylistResponse updatePlaylistPrivacy(Long id, Boolean isPublic, String email) {
        Playlist playlist = validatePlaylistAccess(id, email);
        playlist.setIsPublic(isPublic);
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return PlaylistResponse.fromEntity(updatedPlaylist, baseUrl);
    }

    @Override
    public MessageResponse addSongToPlaylist(Long playlistId, Long songId, String email) {
        Playlist playlist = validatePlaylistAccess(playlistId, email);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + songId));
        if(playlistSongRepository.existsByPlaylist_IdAndSong_Id(playlistId, songId)){
            throw new RuntimeException("Song already exists with id: " + songId);
        }
        List<PlaylistSong> existingSongs = playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(playlistId);
        int newPosition = existingSongs.isEmpty() ? 1 : existingSongs.get(existingSongs.size() - 1).getPosition() + 1;
        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        playlistSong.setPosition(newPosition);
        playlistSongRepository.save(playlistSong);
        return new MessageResponse("Song added to playlist successfully");
    }

    @Override
    public MessageResponse removeSongFromPlaylist(Long playlistId, Long songId, String email) {
        validatePlaylistAccess(playlistId, email);

        PlaylistSong playlistSong = playlistSongRepository.findByPlaylist_IdAndSong_Id(playlistId, songId)
                .orElseThrow(() -> new RuntimeException("Song not found in playlist with id: " + songId));
        int removedPosition = playlistSong.getPosition();
        playlistSongRepository.delete(playlistSong);
        List<PlaylistSong> remainingSongs = playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(playlistId);
        for(PlaylistSong ps: remainingSongs){
            if(ps.getPosition() > removedPosition){
                ps.setPosition(ps.getPosition() - 1);
                playlistSongRepository.save(ps);
            }
        }
        return new MessageResponse("Song removed from playlist successfully");

    }

    @Override
    public MessageResponse reorderSongInPlaylist(Long playlistId, Long songId, Integer newPosition, String email) {
        validatePlaylistAccess(playlistId, email);
        PlaylistSong playlistSong = playlistSongRepository.findByPlaylist_IdAndSong_Id(playlistId, songId)
                .orElseThrow(() -> new RuntimeException("Song not found in playlist with id: " + songId));
        List<PlaylistSong> allSongs = playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(playlistId);
        if(newPosition < 1 || newPosition > allSongs.size()){
            throw new RuntimeException("Invalid new position: " + newPosition);
        }
        int currentPosition = playlistSong.getPosition();
        if(newPosition == currentPosition){
            return new MessageResponse("Song is already at the specified position");
        }
        else if(newPosition > currentPosition){
            for(PlaylistSong ps: allSongs){
                if(ps.getPosition() > currentPosition && ps.getPosition() <= newPosition){
                    ps.setPosition(ps.getPosition() - 1);
                    playlistSongRepository.save(ps);
                }
            }
        }
        else{
            for(PlaylistSong ps: allSongs){
                if(ps.getPosition() < currentPosition && ps.getPosition() >= newPosition){
                    ps.setPosition(ps.getPosition() + 1);
                    playlistSongRepository.save(ps);
                }
            }
        }
        playlistSong.setPosition(newPosition);
        playlistSongRepository.save(playlistSong);
        List<PlaylistSong> updatedSongs = playlistSongRepository.findByPlaylist_IdOrderByPositionAsc(playlistId);
        int normalizedPosition = 1;
        for(PlaylistSong ps: updatedSongs){
            if(ps.getPosition() != normalizedPosition){
                ps.setPosition(normalizedPosition);
                playlistSongRepository.save(ps);
            }
            normalizedPosition++;
        }
        return new MessageResponse("Song position updated successfully to " + newPosition);
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    private Playlist validatePlaylistAccess(Long id, String email) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found with id: " + id));
        AppUser appUser = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        boolean isOwner = playlist.getAppUser().getId().equals(appUser.getId());
        boolean isAdmin = "ADMIN".equals(appUser.getRole());
        if(!isOwner && !isAdmin) {
            throw new RuntimeException("You do not have permission to modify this playlist");
        }
        return playlist;
    }
}
/*
Working of createPlaylist():
1. Retrieve the AppUser entity for the given email (will be used to set the owner of the playlist).
2. Create a new Playlist entity and set its name, description, isPublic status, and owner (AppUser), fetching the values from the PlaylistRequest object.
3. If an image file is provided, generate a unique filename, save the image file to the server, and set the imageUrl field of the Playlist entity.
4. Save the new Playlist entity to the repository.
5. Return the PlaylistResponse object created from the saved Playlist entity.
 */

/*
Working of updatePlaylistPrivacy():
1. Validate the playlist access for the given playlistId and email.
2. Update the isPublic field of the Playlist entity with the provided value.
3. Save the updated Playlist entity to the repository.
4. Return the updated PlaylistResponse object.
 */

/*
Working of addSongToPlaylist():
1. Validate the playlist access for the given playlistId and email.
2. Retrieve the Song entity for the given songId.
3. Check if the song already exists in the playlist using existsByPlaylist_IdAndSong_Id method of PlaylistSongRepository. If it does, throw an exception.
4. Retrieve all existing songs in the playlist ordered by their position (ascending).
5. Determine the new position for the song to be added. If there are no existing songs, set the position to 1. Otherwise, set the position to the last song's position + 1.
6. Create a new PlaylistSong entity and set its playlist, song, and position.
7. Save the new PlaylistSong entity to the repository.
8. Return a message indicating the song has been added to the playlist successfully.
 */

/* Working of removeSongFromPlaylist():
1. Validate the playlist access for the given playlistId and email.
2. Retrieve the PlaylistSong entity for the given playlistId and songId.
3. delete the PlaylistSong entity from the repository.
4. Retrieve all remaining songs in the playlist ordered by their position (ascending).
5. For each remaining song, if its position is greater than the removed song's position, decrement its position by 1 and save the updated entity back to the repository.
6. Return a message indicating the song has been removed from the playlist successfully.
 */

/* Working of reorderSongInPlaylist():
1. Validate the playlist access for the given playlistId and email.
2. Retrieve the PlaylistSong entity for the given playlistId and songId.
3. Retrieve all songs in the playlist ordered by their position (ascending).
4. Check if the newPosition is valid (between 1 and the total number of songs).
5. If the newPosition is the same as the current position, return a message indicating no change is needed.
6. If the newPosition is greater than the current position, decrement the position of all songs that are between the current position and the new position.
7. If the newPosition is less than the current position, increment the position of all songs that are between the new position and the current position.
8. Update the position of the target song to the newPosition.
9. Normalize the positions of all songs in the playlist to ensure they are sequential starting from 1.
10. Return a message indicating the song position has been updated successfully.
 */

