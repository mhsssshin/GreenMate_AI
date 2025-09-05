package com.greenmate.routerecommendation.controller;

import com.greenmate.routerecommendation.dto.GeocodeRequest;
import com.greenmate.routerecommendation.dto.GeocodeResponse;
import com.greenmate.routerecommendation.service.GeocodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Geocode", description = "위치 검색 API")
public class GeocodeController {

    private final GeocodeService geocodeService;

    @PostMapping("/geocode")
    @Operation(
        summary = "키워드 기반 위치 검색", 
        description = "주소, 건물명, 지하철역명 등의 키워드를 통해 위도/경도를 검색합니다."
    )
    public ResponseEntity<GeocodeResponse> geocodeLocation(
            @Valid @RequestBody GeocodeRequest request) {
        
        try {
            log.info("위치 검색 API 호출 - 키워드: {}", request.getKeyword());
            
            GeocodeResponse response = geocodeService.geocodeLocation(request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("위치 검색 API 처리 중 오류 발생", e);
            
            // 에러 응답
            GeocodeResponse errorResponse = new GeocodeResponse(
                "error",
                null,
                "위치 검색 중 오류가 발생했습니다: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}