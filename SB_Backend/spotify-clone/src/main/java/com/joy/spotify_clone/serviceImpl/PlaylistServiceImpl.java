package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.DTO.request.PlaylistRequest;
import com.joy.spotify_clone.DTO.response.PlaylistResponse;
import com.joy.spotify_clone.entity.AppUser;
import com.joy.spotify_clone.entity.Playlist;
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
        Playlist playlist = validatePlaylistRepository(id, email);
        playlist.setIsPublic(isPublic);
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return PlaylistResponse.fromEntity(updatedPlaylist, baseUrl);
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    private Playlist validatePlaylistRepository(Long id, String email) {
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
