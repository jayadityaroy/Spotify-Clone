package com.joy.spotify_clone.serviceImpl;

import com.joy.spotify_clone.DTO.request.SongRequest;
import com.joy.spotify_clone.DTO.response.MessageResponse;
import com.joy.spotify_clone.DTO.response.PaginatedResponse;
import com.joy.spotify_clone.DTO.response.SongAiInsightsResponse;
import com.joy.spotify_clone.DTO.response.SongResponse;
import com.joy.spotify_clone.entity.AppUser;
import com.joy.spotify_clone.entity.Song;
import com.joy.spotify_clone.repository.AppUserRepository;
import com.joy.spotify_clone.repository.PlaylistRepository;
import com.joy.spotify_clone.repository.PlaylistSongRepository;
import com.joy.spotify_clone.repository.SongRepository;
import com.joy.spotify_clone.service.GenericGeminiService;
import com.joy.spotify_clone.service.SongService;
import com.joy.spotify_clone.util.FileHandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
    private PlaylistSongRepository playlistSongRepository;
    @Autowired
    private GenericGeminiService geminiService;

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

    @Override
    public Object getAllSongs(Long userId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Song> songsPage;
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasUserId = userId != null;
        if(hasUserId && hasSearch){
            songsPage = songRepository.findByAppUserIdAndTitleContainingIgnoreCaseOrAppUserIdAndArtistContainingIgnoreCase(userId, search.trim(), userId, search.trim(), pageable);
        }
        else if(hasSearch){
            songsPage = songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(search.trim(), search.trim(), pageable);
        }
        else if(hasUserId){
            songsPage = songRepository.findByAppUserId(userId, pageable);
        }
        else{
            songsPage = songRepository.findAll(pageable);
        }
        List<SongResponse> songResponses = songsPage.getContent().stream()
                .map(song -> SongResponse.fromEntity(song, baseUrl))
                .toList();

        return new PaginatedResponse<>(
                songResponses,
                songsPage.getNumber(),
                songsPage.getSize(),
                songsPage.getTotalElements(),
                songsPage.getTotalPages(),
                songsPage.isLast(),
                songsPage.isFirst()
        );
    }

    @Override
    public SongResponse getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + id));
        return SongResponse.fromEntity(song, baseUrl);
    }

    @Override
    public SongResponse updateSong(Long id, SongRequest request, MultipartFile songFile, MultipartFile imageFile, String email) {
        Song song = validateSongAccess(id, email);
        updateSongMetaData(song, request);
        if(songFile != null && !songFile.isEmpty()){
            deleteOldSongFile(song.getSongUrl());
            String uniqueId = UUID.randomUUID().toString();
            String newSongUrl = processSongFile(songFile, uniqueId);
            song.setSongUrl(newSongUrl);
        }
        if(imageFile != null && !imageFile.isEmpty()){
            deleteOldImageFile(song.getImageUrl());
            String uniqueId = UUID.randomUUID().toString();
            String newImageUrl = processImageFile(imageFile, uniqueId);
            song.setImageUrl(newImageUrl);
        }
        Song updatedSong = songRepository.save(song);
        return SongResponse.fromEntity(updatedSong, baseUrl);
    }

    @Override
    public MessageResponse deleteSong(Long songId, String email) {
        Song song = validateSongAccess(songId, email);
        playlistSongRepository.deleteBySong_Id(songId);
        deleteSongFiles(song);
        songRepository.delete(song);
        return new MessageResponse("Song deleted successfully.");
    }

    @Override
    public SongAiInsightsResponse getSongAiInsights(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + songId));
        String prompt = buildSongAnalysisPrompt(song);
        return geminiService.generateContent(prompt, SongAiInsightsResponse.class);
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private void updateSongMetaData(Song song, SongRequest request) {
        song.setTitle(request.getTitle());
        song.setArtist(request.getArtist());
    }

    private String processSongFile(MultipartFile songFile, String uniqueId) {
        String songExtension = fileHandlerUtil.getFileExtension(songFile.getOriginalFilename());
        String songFileName = uniqueId + songExtension;
        fileHandlerUtil.saveSongFileWithName(songFile, songFileName);
        return "/api/file/song/" + songFileName;
    }

    private String processImageFile(MultipartFile imageFile, String uniqueId) {
        if(imageFile == null || imageFile.isEmpty()){
            return null;
        }
        String imageExtension = fileHandlerUtil.getFileExtension(imageFile.getOriginalFilename());
        String imageFileName = uniqueId + imageExtension;
        fileHandlerUtil.saveImageFileWithName(imageFile, imageFileName);
        return "/api/file/image/" + imageFileName; // Return the URL for accessing the image file using the endpoint defined in FileController
    }

    private void deleteOldSongFile(String songUrl) {
        if(songUrl != null){
            String fileName = fileHandlerUtil.extractFileName(songUrl);
            if(fileName != null){
                fileHandlerUtil.deleteSongFile(fileName);
            }
        }
    }

    private void deleteOldImageFile(String imageUrl) {
        if(imageUrl != null){
            String fileName = fileHandlerUtil.extractFileName(imageUrl);
            if(fileName != null){
                fileHandlerUtil.deleteImageFile(fileName);
            }
        }
    }

    private Song validateSongAccess(Long id, String email) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + id));
        AppUser appUser = getUserByEmail(email);
        boolean isOwner = song.getAppUser().getId().equals(appUser.getId());
        boolean isAdmin = "ADMIN".equals(appUser.getRole());
        if(!isOwner && !isAdmin){
            throw new RuntimeException("You do not have permission to update this song.");
        }
        return song;
    }

    private void deleteSongFiles(Song song) {
        deleteOldSongFile(song.getSongUrl());
        deleteOldImageFile(song.getImageUrl());
    }
    private String buildSongAnalysisPrompt(Song song){
        return String.format("""
                Analyze the song '%s' by '%s' and provide detailed insights in JSON format.
                
                Return a JSON object with the following structure:
                {
                    "analysis": "A detailed 2-3 sentence analysis of the track's musical characteristics, production quality, and emotional
                    impact",
                    "moods": ["List", "of", "4-6", "mood", "keywords"],
                    "genre": "Primary genre classification",
                    "tempo": 120,
                    "key": "Musical key (e.g., C Major, D Minor)",
                    "energy": 7,
                    "similarArtists": ["List", "of", "4-6", "similar", "artists"],
                    "recommendedFor": "A 1-2 sentence recommendation about when and where to listen to this song"
                }
                
                Important:
                - The 'tempo' should be an estimated BPM (beats per minute) between 60-200
                - The 'energy' should be a rating from 1-10
                - Base your analysis on the artist's typical style and the song title
                - Be creative but realistic
                - Return ONLY the JSON object, no additional text
                """, song.getTitle(), song.getArtist());
    }
}
/*
Working of addSong method:
1. The method accepts a SongRequest object(containing title and artist), songFile, imageFile, and the email of the user adding the song.
2. It retrieves the AppUser entity associated with the provided email using the getUserByEmail method (to ensure that the song is linked to the correct user).
3. It generates a unique identifier (UUID) to create unique file names for the song and image files.
4. It creates a new Song entity and sets its metadata (title and artist) using the updateSongMetaData method.
5. It processes the song file and image file using the processSongFile and processImageFile methods, respectively. These methods save the files to the server and return the URLs for accessing them.
6. It sets the songUrl and imageUrl fields of the Song entity with the returned URLs.
7. It saves the Song entity to the database using the songRepository.
8. Finally, it returns a SongResponse DTO created from the saved Song entity using the fromEntity method, which includes the base URL for accessing the song and image files.
 */

/*
Working of getAllSongs method:
1. The method accepts parameters for userId(for filtering by user), page, size, and search(for filtering by title or artist).
2. It creates a Pageable object using the provided page and size.
3. It checks if the search parameter is provided and not empty, and if the userId parameter is provided.
4. Based on the presence of userId and search parameters, it queries the SongRepository to fetch the appropriate page of songs:
   - If both userId and search are provided, it fetches songs that match the userId and contain the search term in either title or artist.
   - If only search is provided, it fetches songs that contain the search term in either title or artist.
   - If only userId is provided, it fetches songs that belong to the specified user.
   - If neither is provided, it fetches all songs.
5. It maps the fetched Song entities to SongResponse DTOs using the fromEntity method.
6. Finally, it returns a PaginatedResponse object containing the list of SongResponse DTOs and pagination metadata (current page, size, total elements, total pages, is last page, is first page).
 */
