package com.greenmate.routerecommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenmate.routerecommendation.dto.GeocodeRequest;
import com.greenmate.routerecommendation.dto.GeocodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodeService {

    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeocodeResponse geocodeLocation(GeocodeRequest request) {
        try {
            log.info("위치 검색 요청 처리 시작 - 키워드: {}, 타입: {}", 
                    request.getKeyword(), request.getLocationType());

            // 기본값 설정
            String region = request.getRegion() != null ? request.getRegion() : "대한민국";
            String locationType = request.getLocationType() != null ? request.getLocationType() : "building";

            // OpenAI API를 통해 위치 검색
            String aiResponse = openAIService.generateGeocodeResult(
                request.getKeyword(),
                locationType,
                region
            );

            // AI 응답에서 위치 정보 추출
            GeocodeResponse.GeocodeData geocodeData = parseGeocodeFromAIResponse(aiResponse);

            GeocodeResponse response = new GeocodeResponse(
                "success",
                geocodeData,
                "위치 검색이 완료되었습니다."
            );

            log.info("위치 검색 완료 - 위도: {}, 경도: {}, 신뢰도: {}", 
                    geocodeData.getLatitude(), geocodeData.getLongitude(), geocodeData.getConfidence());
            
            return response;

        } catch (Exception e) {
            log.error("위치 검색 중 오류 발생", e);
            throw new RuntimeException("위치 검색 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private GeocodeResponse.GeocodeData parseGeocodeFromAIResponse(String aiResponse) {
        try {
            // OpenAI 응답에서 JSON 부분만 추출
            String jsonResponse = extractJsonFromResponse(aiResponse);
            
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            Double latitude = root.path("latitude").asDouble();
            Double longitude = root.path("longitude").asDouble();
            String address = root.path("address").asText();
            Double confidence = root.path("confidence").asDouble();

            // 기본값 검증
            if (latitude == 0.0 && longitude == 0.0) {
                throw new RuntimeException("올바른 위치 정보를 찾을 수 없습니다");
            }

            return new GeocodeResponse.GeocodeData(latitude, longitude, address, confidence);
            
        } catch (Exception e) {
            log.error("AI 응답에서 위치 정보 파싱 중 오류 발생: {}", aiResponse, e);
            throw new RuntimeException("AI 응답을 파싱할 수 없습니다");
        }
    }

    private String extractJsonFromResponse(String response) {
        // OpenAI 응답에서 JSON 부분만 추출 (```json 태그 제거 등)
        String cleaned = response.trim();
        
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        // JSON 시작점 찾기
        int jsonStart = cleaned.indexOf("{");
        if (jsonStart == -1) {
            throw new RuntimeException("JSON 형식을 찾을 수 없습니다");
        }
        
        return cleaned.substring(jsonStart).trim();
    }
}