package com.joy.spotify_clone.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FileHandlerUtil {
    @Value("${file.storage.song.path}")
    private String songStoragePath;

    @Value("${file.storage.image.path}")
    private String imageStoragePath;

    public String saveSongFileWithName(MultipartFile file, String customFileName) {
        return saveSongFileWithCustomName(file, songStoragePath, customFileName, "song");
    }
    public String saveImageFileWithName(MultipartFile file, String customFileName) {
        return saveSongFileWithCustomName(file, imageStoragePath, customFileName, "image");
    }
    private String saveSongFileWithCustomName(MultipartFile file,
                                              String storagePath,
                                              String customFileName,
                                              String fileType) {
        if(file.isEmpty()){
            throw new RuntimeException("Failed to store empty " + fileType + " file.");
        }
        try{
            Path directoryPath = Paths.get(storagePath);
            if(!Files.exists(directoryPath)){
                Files.createDirectories(directoryPath);
            }
            Path destinationPath = directoryPath.resolve(customFileName);
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            return customFileName;
        }catch(IOException ex){
            throw new RuntimeException("Failed to save " + fileType + " file.");
        }
    }

    public Resource loadSongFile(String fileName){
        return loadFile(fileName, songStoragePath);
    }
    public Resource loadImageFile(String fileName){
        return loadFile(fileName, imageStoragePath);
    }

    private Resource loadFile(String fileName, String storagePath) {
        try{
            Path filePath = Paths.get(storagePath).resolve(fileName).normalize();
            Resource resource = new UrlResource((filePath.toUri()));
            if(resource.exists() && resource.isReadable()){
                return resource;
            }else{
                throw new RuntimeException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file: "+fileName, e);
        }
    }

    public void deleteSongFile(String fileName){
        deleteFile(fileName, songStoragePath);
    }
    public void deleteImageFile(String fileName) {
        deleteFile(fileName, imageStoragePath);
    }
    private void deleteFile(String fileName, String storagePath){
        try{
            Path filePath = Paths.get(storagePath).resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        }catch (IOException e){
            throw new RuntimeException("Error deleting file: "+fileName, e);
        }
    }

    public String extractFileName(String url){
        if(url != null && url.contains("/")){
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return null;
    }

    public String getFileExtension(String fileName){
        if(fileName != null && fileName.contains(".")){
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return null;
    }
}
/*
Working of saveSongFileWithCustomName():
1. Empty file check
2. Convert storage path to a Path object:
    - storagePath is probably a string
    - Paths.get(storagePath) converts the string to a Path object, which is the Java NIO way of representing file system paths.
    Why use Path:
    It makes path manipulation safer and more portable than string concatenation.
3. If the target folder doesn’t exist, it creates it.
4. Build final destination path: This combines the folder path and filename into a full file path
5. Copy the file to the destination path, replacing any existing file with the same name:
    file.getInputStream(): Gets a stream of the uploaded file’s bytes.
    destinationPath
    StandardCopyOption.REPLACE_EXISTING : If a file with the same name already exists, overwrite it.
        Why REPLACE_EXISTING matters
            Suppose a file named song_42.mp3 already exists.
            Without REPLACE_EXISTING, the save may fail.
            With it, the old file is replaced by the new one.
 */
/*
Working of loadFile():
1. Build the full file path by combining the storage path and the filename.
   -Paths.get(storagePath): Converts the storage path string to a Path object.
   -resolve(fileName): Appends the filename to the storage path, creating a full path
   -normalize(): Cleans up the path, removing any redundant elements like ".." or "."
2. Convert Path into a URL and then into a Resource object:
   -filePath.toUri(): Converts the Path to a URI, which is a standard way to represent file locations.
   -new UrlResource(filePath.toUri()): Creates a Resource object that points to the file at that URI.
3. Check if the resource exists and is readable:
    -resource.exists(): Checks if the file actually exists on the filesystem.
    -resource.isReadable(): Checks if the file can be read (permissions).
4. If the file is valid, return the Resource object; otherwise, throw an exception.
Why return Resource instead of File
    This is a Spring-friendly approach.
    A Resource can easily be used in controller methods
 */
/*
Working of deleteFile():
1. Build the full file path by combining the storage path and the filename.
2. Use Files.deleteIfExists(filePath) to delete the file if it exists.
   - This method will not throw an exception if the file does not exist, making it safe to call without additional checks.
3. If an IOException occurs during deletion, it is caught and rethrown as a RuntimeException with a descriptive message.
 */
/*
Working of extractFileName():
1. Check if the URL is not null and contains a forward slash ("/").
2. If both conditions are met, use url.lastIndexOf("/") to find the position of the last slash in the URL.
3. Use url.substring(url.lastIndexOf("/") + 1) to extract the substring that comes after the last slash, which is the filename.
4. If the URL is null or does not contain a slash, return null.
 */
/*
Working of getFileExtension():
1. Check if the fileName is not null and contains a dot (".").
2. If both conditions are met, use fileName.lastIndexOf(".") to find the position of the last dot in the filename.
3. Use fileName.substring(fileName.lastIndexOf(".") + 1) to extract the substring that comes after the last dot, which is the file extension.
4. If the fileName is null or does not contain a dot, return null.
 */
