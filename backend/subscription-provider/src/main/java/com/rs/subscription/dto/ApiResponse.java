package com.rs.subscription.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String code;
    private boolean success;
    private String message;
    private T data;
    private Object error;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder().code("0000").success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder().code("0000").success(true).message(message).build();
    }

    public static <T> ApiResponse<T> error(String code, String message, Object error) {
        return ApiResponse.<T>builder().code(code).success(false).message(message).error(error).build();
    }
}
