package com.triple.exception;

import com.triple.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ObjectNotFoundException extends ReviewServiceException{
    public ObjectNotFoundException(String objectName) {
        super(objectName + " Entity not found..", ErrorCode.NOT_FOUND_ENTITY);
    }
}
