package com.triple.repository;

import com.triple.entity.UserEntity;
import com.triple.entity.UserPointDetail;
import com.triple.entity.UserPointEntity;
import com.triple.enums.ErrorCode;
import com.triple.exception.ObjectNotFoundException;
import com.triple.service.ReviewService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    @Test
    @Transactional
    @DisplayName("사용자 계정 생성 테스트")
    void save_user_test() {
        UserEntity userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("TestUser2")
                .build();
        userRepository.save(userEntity);

        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getName()).isEqualTo("TestUser2");
    }

    @Test
    @Transactional
    @DisplayName("사용자 ID 조회 테스트")
    void search_user_id() {
        final Optional<UserEntity> user = userRepository.findById(UUID.fromString("46d2f3d2-4ca9-4e13-bf2a-f8910cbc86b6"));
        user.ifPresent(userEntity ->
                assertThat(userEntity.getName()).isEqualTo("TestUser"));
    }

    @Test
    @Transactional
    @DisplayName("없는 사용자 Id 조회 오류 테스트")
    void not_found_search_user_test() {
        assertThrows(ObjectNotFoundException.class, () -> userRepository.findById(UUID.fromString("46d2f3d2-4ca9-4e13-bf2a-f8910cbc86b0"))
                .orElseThrow(() -> new ObjectNotFoundException("User")));
    }



}
