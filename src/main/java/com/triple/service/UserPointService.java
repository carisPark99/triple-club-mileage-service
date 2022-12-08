package com.triple.service;

import com.triple.dto.ApiResponseDto;
import com.triple.entity.UserEntity;
import com.triple.entity.UserPointEntity;
import com.triple.enums.ErrorCode;
import com.triple.exception.ReviewServiceException;
import com.triple.repository.UserPointRepository;
import com.triple.repository.UserRepository;
import com.triple.vo.RequestSearchPointVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UserPointService {

    UserPointRepository userPointRepository;
    UserRepository userRepository;

    @Autowired
    public UserPointService(UserPointRepository userPointRepository, UserRepository userRepository) {
        this.userPointRepository = userPointRepository;
        this.userRepository = userRepository;
    }

    /**
     * 사용자 리뷰 포인트 조회
     * @param requestSearchPointVo RequestSearchPointVo
     * @return ApiResponseDto
     */
    public ApiResponseDto searchPoint(RequestSearchPointVo requestSearchPointVo) {
        log.debug("●●●●● Start searchpoint ●●●●●");
        // UserId 조회.
        final Optional<UserEntity> user = Optional.ofNullable(
                userRepository.findById(requestSearchPointVo.getUserId())
                        .orElseThrow(() -> new ReviewServiceException("User Id is Not Found", ErrorCode.NOT_FOUND_ENTITY)));

        // 리뷰 포인트 조회
        final Optional<UserPointEntity> userPoint = Optional.ofNullable(userPointRepository.findByUser(user.get())
                .orElseThrow(() -> new NullPointerException("User Point is Not Found")));

        // Map에 담아서 리턴
        Map<String, Object> resultMap = new HashMap<>();
        userPoint.ifPresent(v -> {
            resultMap.put("user-id", user.get().getId());
            resultMap.put("user-name", user.get().getName());
            resultMap.put("user-point", v.getReviewPoint());
        });

        return new ApiResponseDto().resulst(resultMap, HttpStatus.OK);

    }

}
