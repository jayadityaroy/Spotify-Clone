package com.joy.spotify_clone.controller;

import com.google.common.net.HttpHeaders;
import com.joy.spotify_clone.util.FileHandlerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileHandlerUtil fileHandlerUtil;

    @GetMapping("/song/{fileName}")
    public ResponseEntity<?> getSong(@PathVariable String fileName) {
        try{
            Resource resource = fileHandlerUtil.loadSongFile(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"File not found\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        try{
            Resource resource = fileHandlerUtil.loadImageFile(fileName);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"File not found\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }

}
/*
Working of getSong():
1. The method is mapped to the GET request at the endpoint "/song/{fileName}".
2. It takes a fileName as a path variable, which represents the name of the song file to be retrieved.
3. The loadSongFile method of the fileHandlerUtil is called with the provided fileName to load the song file as a Resource.
4. If the file is found, a ResponseEntity is created with the following properties:
   - HTTP status code 200 (OK)
   - Content type set to MediaType.APPLICATION_OCTET_STREAM, indicating that the response contains binary data (the song file).
   - Content-Disposition header tells the browser how to handle the file:
        - "inline" suggests that the browser should try to display the file within the page if possible.
        - "filename=\"" + fileName + "\"" specifies the name of the file being sent, which can be used by the browser when saving the file.
        (Note: The "inline" disposition is used here to allow the browser to play the song directly if it supports it,
        rather than forcing a download. If you want to force a download, you can change "inline" to "attachment".)
   - The body of the response contains the loaded Resource (the song file).
5. If the file is not found or any other exception occurs, a ResponseEntity is created with the following properties:
   - HTTP status code 404 (Not Found)
   - Content type set to MediaType.APPLICATION_JSON, indicating that the response contains JSON data (the error message).
   - The body of the response contains a JSON object with an "error" field set to "File not found" and
   a "message" field containing the exception message."attachment; filename=\"" + fileName + "\""
 */
/*
Working of getImage():
1. The method is mapped to the GET request at the endpoint "/image/{fileName}"
2. It takes a fileName as a path variable, which represents the name of the image file to be retrieved.
3. The loadImageFile method of the fileHandlerUtil is called with the provided fileName to load the image file as a Resource.
4. If the file is found, a ResponseEntity is created with the following properties:
   - HTTP status code 200 (OK)
   - Content type set to MediaType.IMAGE_JPEG, indicating that the response contains JPEG image data. // no need to set content-disposition header for images, as they are typically displayed directly in the browser.
5. If the file is not found or any other exception occurs, a ResponseEntity is created with the following properties:
   - HTTP status code 404 (Not Found)
   - Content type set to MediaType.APPLICATION_JSON, indicating that the response contains JSON data (the error message).
   - The body of the response contains a JSON object with an "error" field set to "File not found" and a "message" field containing the exception message.
 */
