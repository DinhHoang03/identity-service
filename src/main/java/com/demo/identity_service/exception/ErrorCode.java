package com.demo.identity_service.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(1011, "Unknow Exception!"),
    INVALID_KEY(1001, "Invalid Missed Key"),
    USER_EXISTED(1002, "User existed!"),
    USERNAME_INVALID(1003, "Username must be at least 3 characters!"),
    PASSWORD_INVALID(1004, "Password must be 8 characters above!"),
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
