package com.triple.controller;

import com.triple.dto.ApiResponseDto;
import com.triple.enums.ErrorCode;
import com.triple.enums.ReviewActionType;
import com.triple.exception.ReviewServiceException;
import com.triple.service.ReviewService;
import com.triple.vo.RequestDeleteReviewVo;
import com.triple.vo.RequestReviewVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class ReviewController {

    ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> reviewEvent(@Valid @RequestBody final RequestReviewVo requestReviewVo) throws ReviewServiceException {
        final ReviewActionType action = ReviewActionType.valueOf(requestReviewVo.getAction());
        switch (action) {
            // 리뷰생성
            case ADD:
                ApiResponseDto createReview = reviewService.createReview(requestReviewVo);
            return new ResponseEntity<>(createReview, createReview.getHttpStatus());
            // 리뷰 수정
            case MOD:
                ApiResponseDto modReview = reviewService.modReview(requestReviewVo);
                return new ResponseEntity<>(modReview, modReview.getHttpStatus());
                // 리뷰 삭제
            case DELETE:
                RequestDeleteReviewVo requestDeleteReviewVo = RequestDeleteReviewVo.builder()
                        .userId(requestReviewVo.getUserId()).reviewId(requestReviewVo.getReviewId()).build();
                ApiResponseDto deleteReview = reviewService.deleteReview(requestDeleteReviewVo);
                return new ResponseEntity<>(deleteReview, deleteReview.getHttpStatus());
            default:
                throw new ReviewServiceException(ErrorCode.UNKNOWN_REVIEW_ACTION_TYPE);
        }
    }

}
