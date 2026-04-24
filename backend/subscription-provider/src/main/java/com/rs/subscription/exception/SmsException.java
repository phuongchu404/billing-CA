package com.rs.subscription.exception;

import lombok.Getter;

@Getter
public class SmsException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    public SmsException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public static SmsException of(String code, String message, int httpStatus) {
        return new SmsException(code, message, httpStatus);
    }
}
