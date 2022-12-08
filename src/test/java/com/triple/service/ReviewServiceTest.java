package com.triple.service;

import com.triple.entity.*;
import com.triple.enums.ErrorCode;
import com.triple.enums.ReviewType;
import com.triple.exception.DuplicationException;
import com.triple.exception.ReviewServiceException;
import com.triple.repository.*;
import com.triple.vo.RequestReviewVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired UserRepository userRepository;
    @Autowired PlaceRepository placeRepository;
    @Autowired ReviewRepository reviewRepository;
    @Autowired PhotoRepository photoRepository;
    @Autowired UserPointRepository userPointRepository;
    @Autowired UserPointDetailRepository userPointDetailRepository;

    @Test
    void searchUserAndDuplicatePlaceTest() {
        RequestReviewVo requestReviewVo = RequestReviewVo.builder()
                .type(ReviewType.valueOf("REVIEW"))
                .action("ADD")
                .reviewId(UUID.fromString("240a0658-dc5f-4878-9381-ebb7b2667723"))
                .content("...")
                .attachedPhotoIds(List.of(UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb0")))
                .userId(UUID.fromString("9e1094b5-f31c-4118-82fa-5020da938b6d"))
                .placeId(UUID.fromString("ac86b84a-212b-4797-9753-513a8493828c"))
                .build();

        final Optional<UserEntity> user = Optional.ofNullable(
                userRepository.findById(requestReviewVo.getUserId())
                        .orElseThrow(() -> new ReviewServiceException("User Id is Not Found", ErrorCode.NOT_FOUND_ENTITY)));

        // 장소 조회.
        final Optional<PlaceEntity> place = placeRepository.findById(requestReviewVo.getPlaceId());

        // 장소Id와 사용자Id 중복여부 확인.
        final boolean isPlaceUser = reviewRepository
                .existsByPlaceAndUser(place.isPresent() ? place.get().getId() : requestReviewVo.getPlaceId(), user.get());

        assertThat(user.get().getId()).isEqualTo(UUID.fromString("9e1094b5-f31c-4118-82fa-5020da938b6d"));
        assertThat(isPlaceUser).isEqualTo(true);

    }

    @Test
    @Transactional
    @Rollback(value = false)
    void create_review_test() {
        // request param.
        RequestReviewVo requestReviewVo = RequestReviewVo.builder()
                .type(ReviewType.valueOf("REVIEW"))
                .action("ADD")
                .reviewId(UUID.fromString("240a0658-dc5f-4878-9381-ebb7b2667723"))
                .content("...")
                .attachedPhotoIds(List.of(UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb0")))
                .userId(UUID.fromString("9e1094b5-f31c-4118-82fa-5020da938b6d"))
                .placeId(UUID.fromString("ac86b84a-212b-4797-9753-513a8493828c"))
                .build();

        // UserId 조회.
        final Optional<UserEntity> user = Optional.ofNullable(
                userRepository.findById(requestReviewVo.getUserId())
                    .orElseThrow(() -> new ReviewServiceException("User Id is Not Found", ErrorCode.NOT_FOUND_ENTITY)));

        // 장소 조회.
        final Optional<PlaceEntity> place = placeRepository.findById(requestReviewVo.getPlaceId());

        // 장소Id와 사용자Id 중복여부 확인.
        final boolean isPlaceUser = reviewRepository
                .existsByPlaceAndUser(place.isPresent() ? place.get().getId() : requestReviewVo.getPlaceId(), user.get());

        // 첫리뷰 점수.
        int firstReviewPoint = 0;

        if (!isPlaceUser) {
            firstReviewPoint++;
        } else {
            throw new DuplicationException("", ErrorCode.DUPLICATED_OBJECT);
        }

        ReviewEntity review = ReviewEntity.builder()
                .id(requestReviewVo.getReviewId())
                .user(user.get())
                .place(place.isPresent() ? place.get().getId() : requestReviewVo.getPlaceId())
                .content(requestReviewVo.getContent())
                .build();

        // 리뷰 저장
        final ReviewEntity save = reviewRepository.save(review);

        // 사진 갯수 확인.
        int photosLen = requestReviewVo.getAttachedPhotoIds().size();
        // 리뷰 글자 수 확인.
        int contentLen = requestReviewVo.getContent().length();
        // 사진 갯수 확인.
        int photoLen = requestReviewVo.getAttachedPhotoIds().size();

        if (photoLen > 0) {
            // 사진 갯수만큼 리뷰사진테이블에 저장.
            for (int i=0; i < photosLen; i++) {
                PhotoEntity photoEntity = PhotoEntity.builder()
                        .id(UUID.randomUUID())
                        .photoId(requestReviewVo.getAttachedPhotoIds().get(i))
                        .review(save)
                        .name("review-photo-" + i)
                        .build();
                photoRepository.save(photoEntity);
            }
        } else {
            PhotoEntity photoEntity = PhotoEntity.builder()
                    .id(UUID.randomUUID())
                    .review(save)
                    .build();
            photoRepository.save(photoEntity);
        }

        // 총점 확인.
        int totalReviewPoint = firstReviewPoint + (photosLen > 0 ? 1 : 0) + (contentLen > 0 ? 1 : 0);

        // 포인트 정보에 사용자Id 있는지 체크.
        final Optional<UserPointEntity> pointUser = userPointRepository.findByUser(user.get());
        // 사용자Id가 있고, 포인트 점수가 0보다 크다면 포인트 점수 Update.
        if (pointUser.isPresent() && pointUser.get().getReviewPoint() > 0) {
            pointUser.get().updateReviewTotPoint(totalReviewPoint);
        } else {
            // 신규 포인트 적립.
            UserPointEntity userPointEntity = UserPointEntity.builder()
                    .id(UUID.randomUUID())
                    .user(user.get())
                    .reviewPoint(totalReviewPoint)
                    .build();
            userPointRepository.save(userPointEntity);
        }

        // 포인트 정보 조회.
        final Optional<UserPointEntity> point = userPointRepository.findByUser(user.get());
        point.orElseThrow(() -> new NullPointerException("PointId is not found"));

        // 포인트 이력정보 저장.
        UserPointDetail userPointDetail = UserPointDetail.builder()
                .id(UUID.randomUUID())
                .reviewPoint(point.get().getReviewPoint())
                .userPoint(point.get())
                .review(review)
                .build();

        userPointDetailRepository.save(userPointDetail);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    void mod_review_content_test() {
        // request param.
        RequestReviewVo requestReviewVo = RequestReviewVo.builder()
                .type(ReviewType.valueOf("REVIEW"))
                .action("MOD")
                .reviewId(UUID.fromString("240a0658-dc5f-4878-9381-ebb7b2667723"))
                .content("...")
//                .attachedPhotoIds(List.of(UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb9")))
                .attachedPhotoIds(List.of(
                        new UUID[]{
                                UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb4"),
                                UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb5"),
                                UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb6")
                        }))
                .userId(UUID.fromString("9e1094b5-f31c-4118-82fa-5020da938b6d"))
                .placeId(UUID.fromString("ac86b84a-212b-4797-9753-513a8493828c"))
                .build();

        int totalPoint = 0;
        int currentPhotoSize = 0;
        int currentContentLen = 0;

        // UserId 조회.
        final Optional<UserEntity> user = userRepository.findById(requestReviewVo.getUserId());
        user.orElseThrow(() -> new NullPointerException("UserId is not found"));

        // 리뷰 조회.
        final Optional<ReviewEntity> review =
                Optional.ofNullable(reviewRepository.findByUserAndPlace(user.get(), requestReviewVo.getPlaceId())
                        .orElseThrow(() -> new NullPointerException("null..")));

        currentPhotoSize = review.get().getAttachedPhotos().size();

        review.ifPresent(reviewEntity -> reviewEntity.getAttachedPhotos()
                .stream()
                .filter(photo -> !requestReviewVo.getAttachedPhotoIds().contains(photo.getId()))
                .collect(Collectors.toList())
                .forEach(reviewEntity::deletePhoto));

        int requestPhotoSize = requestReviewVo.getAttachedPhotoIds().size();

        if (requestPhotoSize > 0) {
            // 사진 갯수만큼 리뷰사진테이블에 저장.
            for (int i=0; i < requestPhotoSize; i++) {
                PhotoEntity photoEntity = PhotoEntity.builder()
                        .id(UUID.randomUUID())
                        .photoId(requestReviewVo.getAttachedPhotoIds().get(i))
                        .review(review.get())
                        .name("review-photo-" + i)
                        .build();
                photoRepository.save(photoEntity);
            }
        } else {
            PhotoEntity photoEntity = PhotoEntity.builder()
                    .id(UUID.randomUUID())
                    .review(review.get())
                    .build();
            photoRepository.save(photoEntity);
        }

        currentContentLen = review.get().getContent().length();
        review.get().updateContent(requestReviewVo.getContent());

        final Optional<ReviewEntity> reviewEntity = reviewRepository.findById(requestReviewVo.getReviewId());

        if (reviewEntity.isPresent()) {
            final int contentLen = review.get().getContent().length();
            if (contentLen == 0) {
                totalPoint--;
            }
            if (currentContentLen == 0 && contentLen > 0) {
                totalPoint++;
            }
            if (!(currentPhotoSize == requestPhotoSize) && requestPhotoSize > 0) {
                totalPoint++;
            }
            if (requestPhotoSize == 0) {
                totalPoint--;
            }
        }

        final Optional<UserPointEntity> byUser = userPointRepository.findByUser(user.get());
        byUser.orElseThrow(() -> new NullPointerException("PointId is not found"));

        byUser.get().updateReviewTotPoint(totalPoint);

        UserPointDetail userPointDetail = UserPointDetail.builder()
                .id(UUID.randomUUID())
                .reviewPoint(byUser.get().getReviewPoint())
                .userPoint(byUser.get())
                .review(review.get())
                .ddtReviewPoint(totalPoint)
                .build();
        userPointDetailRepository.save(userPointDetail);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    void mod_review_photo() {
        RequestReviewVo requestReviewVo = RequestReviewVo.builder()
                .type(ReviewType.valueOf("REVIEW"))
                .action("MOD")
                .reviewId(UUID.fromString("240a0658-dc5f-4878-9381-ebb7b2667723"))
                .content("...")
//                .attachedPhotoIds(List.of(UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb9")))
                .attachedPhotoIds(List.of(
                        new UUID[]{
                                UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb4"),
                                UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb5"),
                                UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb6")
                        }))
                .userId(UUID.fromString("9e1094b5-f31c-4118-82fa-5020da938b6d"))
                .placeId(UUID.fromString("ac86b84a-212b-4797-9753-513a8493828c"))
                .build();

        final Optional<ReviewEntity> review = Optional.ofNullable(reviewRepository.findById(requestReviewVo.getReviewId())
                .orElseThrow(() -> new NullPointerException("")));

        review.ifPresent(reviewEntity -> reviewEntity.getAttachedPhotos()
                .stream()
                    .filter(photo -> !requestReviewVo.getAttachedPhotoIds().contains(photo.getId()))
                        .collect(Collectors.toList())
                .forEach(reviewEntity::deletePhoto));

        final Set<PhotoEntity> attachedPhotos = review.get().getAttachedPhotos();

        int photoLen = requestReviewVo.getAttachedPhotoIds().size();

        if (photoLen > 0) {
            // 사진 갯수만큼 리뷰사진테이블에 저장.
            for (int i=0; i < photoLen; i++) {
                PhotoEntity photoEntity = PhotoEntity.builder()
                        .id(UUID.randomUUID())
                        .photoId(requestReviewVo.getAttachedPhotoIds().get(i))
                        .review(review.get())
                        .name("review-photo-" + i)
                        .build();
                photoRepository.save(photoEntity);
            }
        } else {
            PhotoEntity photoEntity = PhotoEntity.builder()
                    .id(UUID.randomUUID())
                    .review(review.get())
                    .build();
            photoRepository.save(photoEntity);
        }
    }

    @Test
    @Transactional
    @Rollback(value = false)
    void delete_review_test() {
        UUID id = UUID.fromString("240a0658-dc5f-4878-9381-ebb7b2667723");
        UUID userId = UUID.fromString("9e1094b5-f31c-4118-82fa-5020da938b6d");

        final Optional<ReviewEntity> review = Optional.ofNullable(reviewRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("")));

        int totalPoint = 0;
        int reviewContentSize = 0;
        int reviewPhotoSize = 0;

        if (review.isPresent()) {
            reviewContentSize = review.get().getContent().length();
            reviewPhotoSize = review.get().getAttachedPhotos().size();
            reviewRepository.delete(review.get());
        }

        if (reviewContentSize > 0) {
            totalPoint--;
        }
        if (reviewPhotoSize > 0) {
            totalPoint--;
        }

        // 장소Id와 사용자Id 중복여부 확인.
        final boolean isPlaceUser = reviewRepository
                .existsByPlaceAndUser(review.get().getPlace(), review.get().getUser());

        if (!isPlaceUser) {
            totalPoint--;
        }

        final Optional<UserEntity> user = Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new NullPointerException("")));

        final Optional<UserPointEntity> userPoint = Optional.ofNullable(userPointRepository.findByUser(user.get())
                .orElseThrow(() -> new NullPointerException("")));

        userPoint.get().updateReviewTotPoint(totalPoint);

        UserPointDetail userPointDetail = UserPointDetail.builder()
                .id(UUID.randomUUID())
                .reviewPoint(userPoint.get().getReviewPoint())
                .userPoint(userPoint.get())
                .review(review.get())
                .ddtReviewPoint(totalPoint)
                .build();
        userPointDetailRepository.save(userPointDetail);
    }

    @Test
    void test3() {
        RequestReviewVo requestReviewVo = RequestReviewVo.builder()
                .type(ReviewType.valueOf("REVIEW"))
                .action("ADD")
                .reviewId(UUID.fromString("240a0658-dc5f-4878-9381-ebb7b2667723"))
                .content("...")
                .attachedPhotoIds(List.of(UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb0")))
                .userId(UUID.fromString("9e1094b5-f31c-4118-82fa-5020da938b6d"))
                .placeId(UUID.fromString("ac86b84a-212b-4797-9753-513a8493828c"))
                .build();

        final int size = requestReviewVo.getAttachedPhotoIds().size();
        System.out.println("size ===> "+size);
    }

}
