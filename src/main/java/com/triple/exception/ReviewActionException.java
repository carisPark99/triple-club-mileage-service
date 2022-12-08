package com.triple.exception;

import com.triple.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ReviewActionException extends ReviewServiceException{
    public ReviewActionException(String objectName) {
        super(objectName, ErrorCode.UNKNOWN_REVIEW_ACTION_TYPE);
    }
}
