package com.triple.controller;

import com.triple.config.BeanConfig;
import com.triple.enums.ReviewType;
import com.triple.vo.RequestReviewVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReviewControllerTest {

    @Autowired BeanConfig beanConfig;

    private MockMvc mockMvc;
    private WebApplicationContext ctx;

    @Autowired
    public void setMockMvc(MockMvc mockMvc, WebApplicationContext ctx) {
        this.mockMvc = mockMvc;
        this.ctx = ctx;
    }

    @BeforeEach
    public void mvcEncode() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }

//    @DisplayName("리뷰 생성 테스트")
//    @Transactional(rollbackFor = Exception.class)
//    @Test
//    @Rollback(value = false)
//    void savUser() throws Exception {
//        RequestReviewVo requestReviewVo = RequestReviewVo.builder()
//                .type(ReviewType.valueOf("REVIEW"))
//                .action("ADD")
//                .reviewId(UUID.fromString("92dd8f6c-25ef-46ff-944b-4401ecd09e27"))
//                .attachedPhotoIds(List.of(UUID.fromString("e4d1a64e-a531-46de-88d0-ff0ed70c0bb0")))
//                .userId(UUID.fromString("1e44fa8e-13b4-490f-ba00-c6a834dbe386"))
//                .placeId(UUID.fromString("6c20dbf8-40a9-4dd0-bdab-ac490e960e41"))
//                .content("...")
//                .build();
//
//        // toJson
//        String content = beanConfig.objectMapper().writeValueAsString(requestReviewVo);
//
//        ResultActions resultActions = mockMvc.perform(
//                post("/events")
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                            .accept(MediaType.APPLICATION_JSON_VALUE)
//                            .content(content));
//        // then
//        resultActions.andDo(print())
//                .andExpect(status().is2xxSuccessful())
//                    .andExpect(handler().handlerType(ReviewController.class))
//                    .andExpect(handler().methodName("reviewEvent"))
//                    .andExpect(jsonPath("$.data[0]", is("CREATED_REVIEW")))
//        ;
//    }

}
