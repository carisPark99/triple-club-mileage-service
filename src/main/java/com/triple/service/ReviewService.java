package com.triple.service;

import com.triple.dto.ApiResponseDto;
import com.triple.entity.*;
import com.triple.enums.ErrorCode;
import com.triple.exception.DuplicationException;
import com.triple.exception.ObjectNotFoundException;
import com.triple.repository.*;
import com.triple.vo.RequestReviewVo;
import com.triple.vo.RequestDeleteReviewVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReviewService {

    UserRepository userRepository;
    PlaceRepository placeRepository;
    ReviewRepository reviewRepository;
    PhotoRepository photoRepository;
    UserPointRepository userPointRepository;
    UserPointDetailRepository userPointDetailRepository;

    public ReviewService(UserRepository userRepository, PlaceRepository placeRepository, ReviewRepository reviewRepository,
                         PhotoRepository photoRepository, UserPointRepository userPointRepository,
                         UserPointDetailRepository userPointDetailRepository) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.reviewRepository = reviewRepository;
        this.photoRepository = photoRepository;
        this.userPointRepository = userPointRepository;
        this.userPointDetailRepository = userPointDetailRepository;
    }

    /**
     * 리뷰 생성
     * @param requestReviewVo RequestReviewVo
     * @return ApiResponseDto
     */
    @Transactional
    public ApiResponseDto createReview(RequestReviewVo requestReviewVo) {
        log.debug("●●●●● Start createReview ●●●●●");

        // UserId 조회.
        final UserEntity user = this.getUser(requestReviewVo.getUserId());

        // 장소 조회.
        final Optional<PlaceEntity> place = placeRepository.findById(requestReviewVo.getPlaceId());

        // 장소 중복 여부 boolean field.
        boolean isPlaceUser = false;

        // 장소Id와 사용자Id 중복여부 확인.
        isPlaceUser = reviewRepository.existsByPlaceAndUser(
                place.isPresent() ? place.get().getId() : requestReviewVo.getPlaceId(), user);

        // 첫리뷰 점수.
        int firstReviewPoint = 0;

        // 첫장소이면 첫방문 점수 부여.
        if (!isPlaceUser) {
            firstReviewPoint++;
        } else {
            // 한 사용자는 1개의 장소만 리뷰가 가능하므로 Exception 처리.
            throw new DuplicationException("Place", ErrorCode.DUPLICATED_PLACE);
        }

        ReviewEntity review = ReviewEntity.builder()
                .id(requestReviewVo.getReviewId())
                .user(user)
                // 등록된 장소이면 저장 아닌 경우 요청한 placeId 저장.
                .place(place.isPresent() ? place.get().getId() : requestReviewVo.getPlaceId())
                .content(requestReviewVo.getContent())
                .build();

        // 리뷰 저장
        final ReviewEntity saveReview = reviewRepository.save(review);

        // 사진이 공백이 아닌 경우 갯수만큼 저장
        this.insertUserPhoto(requestReviewVo, saveReview, requestReviewVo.getAttachedPhotoIds().size());

        // 총점 확인.
        int totalReviewPoint =
                // 첫리뷰 점수 + 사진첨부 1장 이상 점수 + 리뷰 글자 1자 이상 잠수 합산
                firstReviewPoint + (requestReviewVo.getAttachedPhotoIds().size() > 0 ? 1 : 0) +
                        (requestReviewVo.getContent().length() > 0 ? 1 : 0);

        // 포인트 정보에 사용자Id 있는지 체크.
        final Optional<UserPointEntity> pointUser = userPointRepository.findByUser(user);

        // 사용자Id가 있고, 포인트 점수가 0보다 크다면 포인트 점수 Update.
        if (pointUser.isPresent()) {
            if (pointUser.get().getReviewPoint() >= 0) {
                pointUser.get().updateReviewTotPoint(totalReviewPoint);
            }
        } else {
            // 사용자Id가 없을 경우 insert.
            userPointRepository.save(new UserPointEntity(UUID.randomUUID(), user, totalReviewPoint));
        }
        // 포인트 정보 조회.
        final Optional<UserPointEntity> point = Optional.ofNullable(userPointRepository.findByUser(user)
                .orElseThrow(() -> new ObjectNotFoundException("UserPoint")));

        UserPointDetail userPointDetail = UserPointDetail.builder()
                .id(UUID.randomUUID())
                .reviewPoint(point.get().getReviewPoint())
                .userPoint(point.get())
                .review(review)
                .build();
        // 포인트 이력정보 저장.
        userPointDetailRepository.save(userPointDetail);

        return new ApiResponseDto().resulst("CREATED_RIVEW", HttpStatus.CREATED);
    }

    /**
     * 리뷰 수정
     * @param requestReviewVo RequestReviewVo
     * @return ApiResponseDto
     */
    @Transactional
    public ApiResponseDto modReview(RequestReviewVo requestReviewVo) {
        log.debug("●●●●● Start modReview ●●●●●");

        int totalPoint = 0; // 총 리뷰 포인트

        // UserId 조회.
        final UserEntity user = this.getUser(requestReviewVo.getUserId());

        // 리뷰 조회.
        final Optional<ReviewEntity> review =
                Optional.ofNullable(reviewRepository.findByUserAndPlace(user, requestReviewVo.getPlaceId())
                        .orElseThrow(() -> new ObjectNotFoundException("Review")));

        final boolean isPhotoEmpty = review.get().getAttachedPhotos().isEmpty();
        final int reviewContenLen = review.get().getContent().length();

        // 기존 사진 삭제.
        review.ifPresent(reviewEntity ->
                reviewEntity.getAttachedPhotos()
                        .stream()
                        .filter(photo -> !requestReviewVo.getAttachedPhotoIds().contains(photo.getId()))
                        .collect(Collectors.toList())
                .forEach(reviewEntity::deletePhoto));

        this.insertUserPhoto(requestReviewVo, review.get(), requestReviewVo.getAttachedPhotoIds().size());

        // 이전 리뷰 내용이 0이 아니고, 요청한 리뷰 내용이 0보다 크면 +
        if (reviewContenLen != 0 && requestReviewVo.getContent().length() > 0) {
            totalPoint++;
        }

        // 이전 사진 첨부가 없었고, 요청한 사진이 1장 이상이면 +
        if (!isPhotoEmpty && requestReviewVo.getAttachedPhotoIds().size() > 1) {
            totalPoint++;
        } else {
            totalPoint--;
        }

        // 요청한 리뷰 내용 update.
        review.get().updateContent(requestReviewVo.getContent());

        // 사용자 포인트 조회.
        final Optional<UserPointEntity> userPoint = Optional.ofNullable(userPointRepository.findByUser(user)
                .orElseThrow(() -> new ObjectNotFoundException("Ponit")));

        // 사용자 포인트 update.
        this.updatePoint(userPoint, totalPoint);

        UserPointDetail userPointDetail = UserPointDetail.builder()
                .id(UUID.randomUUID())
                .reviewPoint(userPoint.get().getReviewPoint())
                .userPoint(userPoint.get())
                .review(review.get())
                .ddtReviewPoint(totalPoint)
                .build();
        // 사용자 포인트 상세 저장.
        userPointDetailRepository.save(userPointDetail);

        return new ApiResponseDto().resulst("UPDATED", HttpStatus.OK);
    }

    /**
     * 리뷰 삭제
     * @param requestDeleteReviewVo RequestDeleteReviewVo
     * @return ApiResponseDto
     */
    @Transactional
    public ApiResponseDto deleteReview(RequestDeleteReviewVo requestDeleteReviewVo) {
        log.debug("●●●●● Start deleteReview ●●●●●");

        // UserId 조회.
        final UserEntity user = this.getUser(requestDeleteReviewVo.getUserId());

        final Optional<ReviewEntity> review = Optional.ofNullable(reviewRepository.findByUserAndId(user, requestDeleteReviewVo.getReviewId())
                .orElseThrow(() -> new ObjectNotFoundException("Review")));

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

        final Optional<UserPointEntity> userPoint = Optional.ofNullable(userPointRepository.findByUser(user)
                .orElseThrow(() -> new ObjectNotFoundException("UserPoint")));

        this.updatePoint(userPoint, totalPoint);

        UserPointDetail userPointDetail = UserPointDetail.builder()
                .id(UUID.randomUUID())
                .reviewPoint(userPoint.get().getReviewPoint())
                .userPoint(userPoint.get())
                .review(review.get())
                .ddtReviewPoint(totalPoint)
                .build();
        userPointDetailRepository.save(userPointDetail);

        return new ApiResponseDto().resulst("DELETED", HttpStatus.OK);
    }

    private void updatePoint(Optional<UserPointEntity> userPoint, int totalPoint) {
        if (userPoint.isPresent()) {
            if (userPoint.get().getReviewPoint() >= 0) {
                userPoint.get().updateReviewTotPoint(totalPoint);
            }
        }
    }

    private UserEntity getUser(UUID userId) {
        final Optional<UserEntity> user = Optional.ofNullable(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ObjectNotFoundException("User")));
        return user.orElseGet(() -> UserEntity.builder().build());
    }

    private void insertUserPhoto(RequestReviewVo requestReviewVo, ReviewEntity save, int photoSize) {
        // 사진이 공백이 아닌 경우 갯수만큼 저장
        if (!requestReviewVo.getAttachedPhotoIds().isEmpty()) {
            // 사진 갯수만큼 리뷰사진테이블에 저장.
            for (int i = 0; i < photoSize; i++) {
                PhotoEntity photoEntity = PhotoEntity.builder()
                        .id(UUID.randomUUID())
                        .photoId(requestReviewVo.getAttachedPhotoIds().get(i))
                        .review(save)
                        .name("example-photo-" + i)
                        .build();
                photoRepository.save(photoEntity);
            }
            // 없을 경우 null 저장
        } else {
            PhotoEntity photoEntity = PhotoEntity.builder()
                    .id(UUID.randomUUID())
                    .review(save)
                    .build();
            photoRepository.save(photoEntity);
        }
    }

}
