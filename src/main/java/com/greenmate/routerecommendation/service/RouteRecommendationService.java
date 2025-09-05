package com.greenmate.routerecommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenmate.routerecommendation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteRecommendationService {

    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RouteRecommendationResponse recommendRoutes(RouteRecommendationRequest request) {
        try {
            log.info("도보 경로 추천 요청 처리 시작 - 사용자: {}, 위치: ({}, {}), 잔여걸음수: {}", 
                    request.getUserId(), request.getLatitude(), request.getLongitude(), request.getRemainingSteps());

            // OpenAI API를 통해 경로 추천 받기
            String aiResponse = openAIService.generateRouteRecommendationNew(
                request.getLatitude(),
                request.getLongitude(),
                request.getRemainingSteps(),
                request.getUserProfile()
            );

            // JSON 응답 파싱
            List<RouteRecommendationResponse.SimpleRoute> routes = parseSimpleRoutesFromAIResponse(aiResponse);

            // 응답 객체 생성
            RouteRecommendationResponse.RouteData routeData = 
                new RouteRecommendationResponse.RouteData(routes);

            RouteRecommendationResponse response = new RouteRecommendationResponse(
                "success",
                routeData,
                "경로 추천이 완료되었습니다."
            );

            log.info("경로 추천 완료 - {} 개의 경로 생성", routes.size());
            return response;

        } catch (Exception e) {
            log.error("경로 추천 중 오류 발생", e);
            throw new RuntimeException("경로 추천 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private List<RouteRecommendationResponse.SimpleRoute> parseSimpleRoutesFromAIResponse(String aiResponse) {
        try {
            // OpenAI 응답에서 JSON 부분만 추출
            String jsonResponse = extractJsonFromResponse(aiResponse);
            
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routesNode = root.path("routes");
            
            List<RouteRecommendationResponse.SimpleRoute> routes = new ArrayList<>();
            
            for (JsonNode routeNode : routesNode) {
                RouteRecommendationResponse.SimpleRoute route = new RouteRecommendationResponse.SimpleRoute();
                route.setType(routeNode.path("type").asText());
                route.setDistance(routeNode.path("distance").asDouble());
                route.setDuration(routeNode.path("duration").asInt());
                route.setSteps(routeNode.path("steps").asInt());
                route.setDescription(routeNode.path("description").asText());
                
                routes.add(route);
            }
            
            if (routes.size() != 3) {
                throw new RuntimeException("3개의 경로가 생성되지 않았습니다. 생성된 경로 수: " + routes.size());
            }
            
            return routes;
            
        } catch (Exception e) {
            log.error("AI 응답 파싱 중 오류 발생: {}", aiResponse, e);
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