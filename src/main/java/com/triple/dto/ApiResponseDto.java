package com.triple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiResponseDto {
    private HttpStatus httpStatus;
    private List<Object> data;

    public ApiResponseDto resulst(Object data, HttpStatus status) {
        return ApiResponseDto.builder()
                .httpStatus(status)
                .data(List.of(data))
                .build();
    }
}
