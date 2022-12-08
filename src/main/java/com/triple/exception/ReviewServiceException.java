package com.triple.exception;

import com.triple.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReviewServiceException extends RuntimeException{

    private final ErrorCode errorCode;

    public ReviewServiceException(String msg, ErrorCode errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

}
