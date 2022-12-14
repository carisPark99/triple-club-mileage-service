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

        // ์ฅ์ ์กฐํ.
        final Optional<PlaceEntity> place = placeRepository.findById(requestReviewVo.getPlaceId());

        // ์ฅ์Id์ ์ฌ์ฉ์Id ์ค๋ณต์ฌ๋ถ ํ์ธ.
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

        // UserId ์กฐํ.
        final Optional<UserEntity> user = Optional.ofNullable(
                userRepository.findById(requestReviewVo.getUserId())
                    .orElseThrow(() -> new ReviewServiceException("User Id is Not Found", ErrorCode.NOT_FOUND_ENTITY)));

        // ์ฅ์ ์กฐํ.
        final Optional<PlaceEntity> place = placeRepository.findById(requestReviewVo.getPlaceId());

        // ์ฅ์Id์ ์ฌ์ฉ์Id ์ค๋ณต์ฌ๋ถ ํ์ธ.
        final boolean isPlaceUser = reviewRepository
                .existsByPlaceAndUser(place.isPresent() ? place.get().getId() : requestReviewVo.getPlaceId(), user.get());

        // ์ฒซ๋ฆฌ๋ทฐ ์?์.
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

        // ๋ฆฌ๋ทฐ ์?์ฅ
        final ReviewEntity save = reviewRepository.save(review);

        // ์ฌ์ง ๊ฐฏ์ ํ์ธ.
        int photosLen = requestReviewVo.getAttachedPhotoIds().size();
        // ๋ฆฌ๋ทฐ ๊ธ์ ์ ํ์ธ.
        int contentLen = requestReviewVo.getContent().length();
        // ์ฌ์ง ๊ฐฏ์ ํ์ธ.
        int photoLen = requestReviewVo.getAttachedPhotoIds().size();

        if (photoLen > 0) {
            // ์ฌ์ง ๊ฐฏ์๋งํผ ๋ฆฌ๋ทฐ์ฌ์งํ์ด๋ธ์ ์?์ฅ.
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

        // ์ด์? ํ์ธ.
        int totalReviewPoint = firstReviewPoint + (photosLen > 0 ? 1 : 0) + (contentLen > 0 ? 1 : 0);

        // ํฌ์ธํธ ์?๋ณด์ ์ฌ์ฉ์Id ์๋์ง ์ฒดํฌ.
        final Optional<UserPointEntity> pointUser = userPointRepository.findByUser(user.get());
        // ์ฌ์ฉ์Id๊ฐ ์๊ณ?, ํฌ์ธํธ ์?์๊ฐ 0๋ณด๋ค ํฌ๋ค๋ฉด ํฌ์ธํธ ์?์ Update.
        if (pointUser.isPresent() && pointUser.get().getReviewPoint() > 0) {
            pointUser.get().updateReviewTotPoint(totalReviewPoint);
        } else {
            // ์?๊ท ํฌ์ธํธ ์?๋ฆฝ.
            UserPointEntity userPointEntity = UserPointEntity.builder()
                    .id(UUID.randomUUID())
                    .user(user.get())
                    .reviewPoint(totalReviewPoint)
                    .build();
            userPointRepository.save(userPointEntity);
        }

        // ํฌ์ธํธ ์?๋ณด ์กฐํ.
        final Optional<UserPointEntity> point = userPointRepository.findByUser(user.get());
        point.orElseThrow(() -> new NullPointerException("PointId is not found"));

        // ํฌ์ธํธ ์ด๋?ฅ์?๋ณด ์?์ฅ.
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

        // UserId ์กฐํ.
        final Optional<UserEntity> user = userRepository.findById(requestReviewVo.getUserId());
        user.orElseThrow(() -> new NullPointerException("UserId is not found"));

        // ๋ฆฌ๋ทฐ ์กฐํ.
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
            // ์ฌ์ง ๊ฐฏ์๋งํผ ๋ฆฌ๋ทฐ์ฌ์งํ์ด๋ธ์ ์?์ฅ.
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
            // ์ฌ์ง ๊ฐฏ์๋งํผ ๋ฆฌ๋ทฐ์ฌ์งํ์ด๋ธ์ ์?์ฅ.
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

        // ์ฅ์Id์ ์ฌ์ฉ์Id ์ค๋ณต์ฌ๋ถ ํ์ธ.
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
