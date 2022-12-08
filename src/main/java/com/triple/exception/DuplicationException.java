package com.triple.exception;

import com.triple.enums.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicationException extends ReviewServiceException{
    public DuplicationException(String objectName, ErrorCode errorCode) {
        super(objectName + " Object already exists..", errorCode);
    }
}
