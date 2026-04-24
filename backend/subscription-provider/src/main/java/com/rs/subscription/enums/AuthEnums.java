package com.rs.subscription.enums;

import com.rs.subscription.exception.ErrorCodes;
import com.rs.subscription.exception.SmsException;

public final class AuthEnums {

    private AuthEnums() {
    }

    public enum AuthProvider {
        LOCAL,
        SSO
    }

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        LOCKED
    }

    public enum TokenStatus {
        ACTIVE,
        REVOKED,
        EXPIRED
    }

    public static <E extends Enum<E>> String normalize(String rawValue, Class<E> enumClass, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, rawValue.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new SmsException(
                ErrorCodes.VALIDATION_FAILED,
                "Invalid value for " + fieldName + ": " + rawValue,
                400
            );
        }
    }
}
