package com.joy.spotify_clone.exception;

import jakarta.validation.constraints.Email;

public class EmailAlreadyExistException extends RuntimeException{
    public EmailAlreadyExistException(String message) {
        super(message);
    }
}
/*
Checked exceptions:
Extend Exception (but not RuntimeException)
Compiler forces handling: either try-catch or throws
Used when caller can realistically recover
Example:
public void readFile() throws IOException { ... }

Unchecked exceptions:
Extend RuntimeException
Compiler does not force handling
Used for programming errors or business rule violations

When to use which exception type:
Use checked when:
Failure is expected from outside world: file system, network, third-party service
Caller has a meaningful recovery path (retry, fallback, alternate input)

Use unchecked when:
Input/state violates business rules (email already exists, invalid playlist id)
Programmer errors (null where not allowed, illegal arguments)
You want cleaner service/controller code and handle centrally (@ControllerAdvice in Spring)*/