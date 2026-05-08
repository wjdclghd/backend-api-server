package com.jch.backendapi.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_REFRESH_TOKEN", "Refresh Token이 올바르지 않습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "AUTH_INACTIVE_USER", "비활성화된 사용자입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
