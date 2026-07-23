package com.joy.spotify_clone.exception;

import com.joy.spotify_clone.DTO.response.ErrorResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class, TokenExpiredException.class})
    public ResponseEntity<com.joy.spotify_clone.DTO.response.ErrorResponse> handleUnauthorizedException(RuntimeException ex, WebRequest request){
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request){
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError)error).getField()+": "+error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                getPath(request),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request){
        logger.error("Runtime exception occured: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request){
        logger.error("Unexpected exception occured: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
   }

    private ResponseEntity<com.joy.spotify_clone.DTO.response.ErrorResponse> buildErrorResponse(Exception ex, HttpStatus httpStatus, WebRequest request) {
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
in, logger.error("Unexpected exception occured: {}", ex.getMessage(), ex);
The "{}" placeholder is replaced by the value of ex.getMessage().
Passing ex as the last argument tells SLF4J to also print the full stack trace after the message.
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
Difference between WebRequest and HttpRequest:

Both HttpServletRequest and WebRequest are objects that hold that information — they're just two different "packaging styles" for the same delivery.

Analogy: Original document vs. a photocopy with a summary

-> HttpServletRequest is like getting the original,
   full legal document — every single detail is in there,
   but it's tied to a very specific format (the Servlet API — the old low-level Java web standard that Tomcat, your server, is built on).
   It's powerful, but verbose and locked to that one "paperwork format."
-> WebRequest is like getting a simplified summary sheet that Spring made from that original document — same core information,
   but wrapped in a cleaner, simpler interface that Spring itself defines.
   It's not tied specifically to Servlets underneath — meaning Spring could theoretically run on a different underlying tech (not just Servlets)
   and your code wouldn't have to change at all.
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