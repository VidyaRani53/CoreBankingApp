package com.banksphere.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Instant timestamp;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<String> error(String message) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);   // mark as failure
        response.setMessage(message); // include the error message
        response.setData(null);       // no data for errors
        return response;
    }

    private void setSuccess(boolean b) {

    }


}