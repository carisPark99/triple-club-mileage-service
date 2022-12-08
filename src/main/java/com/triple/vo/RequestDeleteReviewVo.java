package com.triple.vo;

import lombok.*;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDeleteReviewVo {
    private UUID userId;
    private UUID reviewId;
}
