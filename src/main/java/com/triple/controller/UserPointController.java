package com.triple.controller;

import com.triple.dto.ApiResponseDto;
import com.triple.service.UserPointService;
import com.triple.vo.RequestSearchPointVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserPointController {

    UserPointService userPointService;

    public UserPointController(UserPointService userPointService) {
        this.userPointService = userPointService;
    }

    @GetMapping("/point")
    public ResponseEntity<?> searchPoint(@RequestBody @Valid RequestSearchPointVo requestSearchPointVo) {
        ApiResponseDto searchPoint = userPointService.searchPoint(requestSearchPointVo);
        return new ResponseEntity<>(searchPoint, searchPoint.getHttpStatus());
    }
}
