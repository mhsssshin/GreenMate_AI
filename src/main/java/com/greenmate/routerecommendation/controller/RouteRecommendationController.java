package com.greenmate.routerecommendation.controller;

import com.greenmate.routerecommendation.dto.RouteRecommendationRequest;
import com.greenmate.routerecommendation.dto.RouteRecommendationResponse;
import com.greenmate.routerecommendation.service.RouteRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
@Tag(name = "Route Recommendation", description = "도보 경로 추천 API (PRD 명세)")
public class RouteRecommendationController {

    private final RouteRecommendationService routeRecommendationService;

    @PostMapping("/routes")
    @Operation(
        summary = "도보 경로 추천", 
        description = "사용자의 현재 위치와 잔여 걸음수, 개인 프로필을 기반으로 3가지 도보 경로를 추천합니다."
    )
    public ResponseEntity<RouteRecommendationResponse> recommendRoutes(
            @Valid @RequestBody RouteRecommendationRequest request) {
        
        try {
            log.info("도보 경로 추천 API 호출 - 사용자: {}", request.getUserId());
            
            RouteRecommendationResponse response = routeRecommendationService.recommendRoutes(request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("도보 경로 추천 API 처리 중 오류 발생", e);
            
            // 에러 응답 - 기본 경로 제공
            List<RouteRecommendationResponse.SimpleRoute> defaultRoutes = createDefaultRoutes(request.getRemainingSteps());
            
            RouteRecommendationResponse.RouteData errorData = 
                new RouteRecommendationResponse.RouteData(defaultRoutes);
            
            RouteRecommendationResponse errorResponse = new RouteRecommendationResponse(
                "error",
                errorData,
                "경로 추천 중 오류가 발생했습니다: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private List<RouteRecommendationResponse.SimpleRoute> createDefaultRoutes(Integer remainingSteps) {
        List<RouteRecommendationResponse.SimpleRoute> defaultRoutes = new ArrayList<>();
        
        // Less Walk (50% of remaining steps)
        int lessSteps = (int) (remainingSteps * 0.5);
        double lessDistance = lessSteps * 0.7 / 1000; // km
        int lessDuration = (int) (lessDistance * 12); // 5km/h walking speed
        
        defaultRoutes.add(new RouteRecommendationResponse.SimpleRoute(
            "Less Walk", lessDistance, lessDuration, lessSteps, "가까운 거리 산책 코스"
        ));
        
        // Recommended (90% of remaining steps)
        int recSteps = (int) (remainingSteps * 0.9);
        double recDistance = recSteps * 0.7 / 1000;
        int recDuration = (int) (recDistance * 12);
        
        defaultRoutes.add(new RouteRecommendationResponse.SimpleRoute(
            "Recommended", recDistance, recDuration, recSteps, "적정 운동량 산책 코스"
        ));
        
        // More Walk (120% of remaining steps)
        int moreSteps = (int) (remainingSteps * 1.2);
        double moreDistance = moreSteps * 0.7 / 1000;
        int moreDuration = (int) (moreDistance * 12);
        
        defaultRoutes.add(new RouteRecommendationResponse.SimpleRoute(
            "More Walk", moreDistance, moreDuration, moreSteps, "활발한 운동 산책 코스"
        ));
        
        return defaultRoutes;
    }
}