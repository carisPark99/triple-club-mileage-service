package com.triple.vo;

import com.triple.enums.ReviewActionType;
import com.triple.enums.ReviewType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RequestReviewVo {

    /* 리뷰 타입 */
    @NotNull(message = "Type field is required")
    private ReviewType type;

    /* 리부 요청 타입 */
    @NotNull(message = "Action field is required")
    private String action;

    /* 리뷰Id */
    @NotNull(message = "Review field is required")
    private UUID reviewId;

    /* 리뷰 내용 */
    private String content;

    /* 리뷰사진 */
    private List<UUID> attachedPhotoIds = new ArrayList<>();

    /* 사용자Id */
    @NotNull(message = "UserId field is required")
    private UUID userId;

    /* 장소Id */
    @NotNull(message = "PlaceId field is required")
    private UUID placeId;

}
