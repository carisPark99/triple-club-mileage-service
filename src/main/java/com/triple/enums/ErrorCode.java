package com.triple.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    UNKNOWN_EXCEPTION(500, "E0000", "Unknown Exception"),
    DUPLICATED_OBJECT(400, "E0001", "Duplicated Object"),
    DUPLICATED_PLACE(400, "E0002", "Duplicated Place"),
    NOT_FOUND_ENTITY(404, "E0003", "Entity not found"),
    REVIRE_SERVICE_BAD_REQUEST(404, "E0004", "Review Service bad request"),
    UNKNOWN_REVIEW_ACTION_TYPE(400, "E0003", "Requested review action does not exist")
    ;

    private final int errorStatus;
    private final String errorCode;
    private final String errorMessage;
}
