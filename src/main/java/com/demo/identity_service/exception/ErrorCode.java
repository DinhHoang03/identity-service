package com.demo.identity_service.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1011, "Unknow Exception!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid Missed Key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "User not existed!", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1004, "Username must be at least 3 characters!", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1005, "Password must be 8 characters above!", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Access denied", HttpStatus.FORBIDDEN)
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
