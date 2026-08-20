package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.DTO.request.SongRequest;
import com.joy.spotify_clone.DTO.response.SongResponse;
import com.joy.spotify_clone.entity.AppUser;
import com.joy.spotify_clone.entity.Song;
import com.joy.spotify_clone.repository.AppUserRepository;
import com.joy.spotify_clone.repository.PlaylistRepository;
import com.joy.spotify_clone.repository.SongRepository;
import com.joy.spotify_clone.service.SongService;
import com.joy.spotify_clone.util.FileHandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class SongServiceImpl implements SongService {
    @Autowired
    private FileHandlerUtil fileHandlerUtil;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PlaylistRepository playlistRepository;
    // GenericGeminiService
    @Value("${app.base.url}")
    private String baseUrl;
    @Override
    public SongResponse addSong(SongRequest request, MultipartFile songFile, MultipartFile imageFile, String email) {
        AppUser appUser = getUserByEmail(email);
        String uniqueId = UUID.randomUUID().toString();

        Song song = new Song();
        song.setAppUser(appUser);
        updateSongMetaData(song, request);

        String songUrl = processSongFile(songFile, uniqueId);
        song.setSongUrl(songUrl);
        String imageUrl = processImageFile(imageFile, uniqueId);
        song.setImageUrl(imageUrl);
        Song savedSong = songRepository.save(song);
        return SongResponse.fromEntity(savedSong, baseUrl);
    }

    private String processImageFile(MultipartFile imageFile, String uniqueId) {
        if(imageFile == null || imageFile.isEmpty()){
            return null;
        }
        String imageExtension = fileHandlerUtil.getFileExtension(imageFile.getOriginalFilename());
        String imageFileName = uniqueId + imageExtension;
        fileHandlerUtil.saveImageFileWithName(imageFile, imageFileName);
        return "/api/file/image/" + imageFileName;
    }

    private String processSongFile(MultipartFile songFile, String uniqueId) {
        String songExtension = fileHandlerUtil.getFileExtension(songFile.getOriginalFilename());
        String songFileName = uniqueId + songExtension;
        fileHandlerUtil.saveSongFileWithName(songFile, songFileName);
        return "/api/file/song/" + songFileName;
    }

    private void updateSongMetaData(Song song, SongRequest request) {
        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
