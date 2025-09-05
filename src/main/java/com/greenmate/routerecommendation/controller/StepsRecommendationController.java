package com.greenmate.routerecommendation.controller;

import com.greenmate.routerecommendation.dto.StepsRecommendationRequest;
import com.greenmate.routerecommendation.dto.StepsRecommendationResponse;
import com.greenmate.routerecommendation.service.StepsRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
@Tag(name = "Steps Recommendation", description = "걸음수 추천 API")
public class StepsRecommendationController {

    private final StepsRecommendationService stepsRecommendationService;

    @PostMapping("/steps")
    @Operation(
        summary = "개인 맞춤형 걸음수 추천", 
        description = "사용자의 개인 정보(성별, 나이, 키, 몸무게)를 기반으로 일일 권장 걸음수를 추천합니다."
    )
    public ResponseEntity<StepsRecommendationResponse> recommendSteps(
            @Valid @RequestBody StepsRecommendationRequest request) {
        
        try {
            log.info("걸음수 추천 API 호출 - 사용자: {}", request.getUserId());
            
            StepsRecommendationResponse response = stepsRecommendationService.recommendSteps(request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("걸음수 추천 API 처리 중 오류 발생", e);
            
            // 에러 응답
            StepsRecommendationResponse.StepsData errorData = 
                new StepsRecommendationResponse.StepsData(8000); // 기본값
            
            StepsRecommendationResponse errorResponse = new StepsRecommendationResponse(
                "error",
                errorData
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}