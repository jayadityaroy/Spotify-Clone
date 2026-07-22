package com.joy.spotify_clone.exception;

import com.joy.spotify_clone.DTO.response.ErrorResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void handleClientAbortException(Exception ex, WebRequest request){
        logger.debug("Client disconnected during file streaming:; {}", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<com.joy.spotify_clone.DTO.response.ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistException ex, WebRequest request){
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    private ResponseEntity<com.joy.spotify_clone.DTO.response.ErrorResponse> buildErrorResponse(EmailAlreadyExistException ex, HttpStatus httpStatus, WebRequest request) {
        com.joy.spotify_clone.DTO.response.ErrorResponse errorResponse = new com.joy.spotify_clone.DTO.response.ErrorResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                ex.getMessage(),
                getPath(request) // extract the request path from the WebRequest object
        );
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    private String getPath(WebRequest request){
        return request.getDescription(false).replace("uri=", "");
    }
}
/*
@RestContrellerAdvice:
 -> It's a global exception handler for your whole application.
 -> It intercepts exceptions thrown from any @RestController (so your EmailAlreadyExistException, ResourceNotFoundException, InvalidTokenException, etc. can all be caught here).
 -> It's really @ControllerAdvice + @ResponseBody combined — meaning whatever you return gets automatically serialized to JSON (perfect for a REST API, unlike plain @ControllerAdvice which is meant for views/HTML).
 -> Inside it, you write methods annotated with @ExceptionHandler(SomeException.class) — each one says "if this specific exception happens anywhere, run this method and return this response."
*/

/*
Why ClientAbortException.class, AsyncRequestNotUsableException.class?
-> What they are: Exceptions thrown when a client (browser/user) disconnects while the server is still writing a response — e.g., user closes tab mid-song-stream, wifi drops, or they skip to the next song before the current one loads.
-> Why they happen: Server tries to write more response bytes, but there's no client left to receive them — like pouring water into a cup that just got pulled away.
-> Difference between the two:
    ClientAbortException → thrown when the disconnect happens during normal/synchronous request handling.
    AsyncRequestNotUsableException → thrown when it happens during async request handling (Spring's mechanism for freeing up threads on long-running tasks like file streaming).
-> How they're similar: Both represent the same real-world event (client left) — just detected by different layers of the stack depending on which mode the server was in.
-> Why handle them together: You don't care how Spring detected it — you just want the same reaction: log quietly (.debug(), not .error()) since it's not a bug, just normal client behavior, and move on.
 */

/*
In getPath():
 -> request.getDescription(false)
    Returns a string describing the current request.
    In Spring, this often looks like:
    "uri=/api/users/1"
    The false means: do not include session info.
 -> .replace("uri=", "")
    Removes the "uri=" prefix.
 -> So:
    "uri=/api/users/1" -> "/api/users/1"
 */