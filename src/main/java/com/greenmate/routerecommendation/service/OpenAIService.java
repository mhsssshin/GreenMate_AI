package com.greenmate.routerecommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenmate.routerecommendation.dto.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.temperature}")
    private double temperature;

    @Value("${gemini.max-output-tokens}")
    private int maxOutputTokens;

    public OpenAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // 걸음수 추천을 위한 새로운 메서드
    public String generateStepsRecommendation(String gender, Integer age, Integer height, Integer weight) {
        try {
            String prompt = buildStepsPrompt(gender, age, height, weight);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Gemini 걸음수 추천 API 호출 중 오류 발생", e);
            throw new RuntimeException("걸음수 추천 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 위치 검색을 위한 새로운 메서드
    public String generateGeocodeResult(String keyword, String locationType, String region) {
        try {
            String prompt = buildGeocodePrompt(keyword, locationType, region);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Gemini 위치 검색 API 호출 중 오류 발생", e);
            throw new RuntimeException("위치 검색 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 새로운 도보 경로 추천 메서드
    public String generateRouteRecommendationNew(double latitude, double longitude, 
                                                Integer remainingSteps, UserProfile userProfile) {
        try {
            String prompt = buildRoutePromptNew(latitude, longitude, remainingSteps, userProfile);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Gemini 도보 경로 추천 API 호출 중 오류 발생", e);
            throw new RuntimeException("도보 경로 추천 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 기존 메서드 (하위 호환성을 위해 유지)
    public String generateRouteRecommendation(double originLat, double originLng, 
                                            double destLat, double destLng, 
                                            Integer walkRecommendCount, 
                                            Integer timeLimitMinutes) {
        try {
            String prompt = buildPrompt(originLat, originLng, destLat, destLng, 
                                      walkRecommendCount, timeLimitMinutes);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            throw new RuntimeException("경로 추천 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String buildPrompt(double originLat, double originLng, 
                              double destLat, double destLng, 
                              Integer walkRecommendCount, 
                              Integer timeLimitMinutes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 GreenMate의 경로 추천 AI입니다. ");
        prompt.append("다음 조건에 따라 3가지 경로(short, recommended, long)를 JSON 형태로 추천해주세요.\n\n");
        
        prompt.append("출발지: 위도 ").append(originLat).append(", 경도 ").append(originLng).append("\n");
        prompt.append("목적지: 위도 ").append(destLat).append(", 경도 ").append(destLng).append("\n");
        
        if (walkRecommendCount != null) {
            prompt.append("추천 걸음수: ").append(walkRecommendCount).append("걸음\n");
        }
        
        if (timeLimitMinutes != null) {
            prompt.append("제한 시간: ").append(timeLimitMinutes).append("분\n");
        }
        
        prompt.append("\n경로 유형:\n");
        prompt.append("- short: 최단 경로 (지하철/버스 많이 이용)\n");
        prompt.append("- recommended: 균형 잡힌 경로 (적당한 도보 + 대중교통)\n");
        prompt.append("- long: 산책 경로 (도보 위주)\n\n");
        
        prompt.append("걸음 수 계산: steps = (distance_km * 1000) / 0.7\n\n");
        
        prompt.append("다음 JSON 형식으로 정확히 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"routes\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"short\",\n");
        prompt.append("      \"distance_km\": 1.2,\n");
        prompt.append("      \"duration_minutes\": 15,\n");
        prompt.append("      \"steps\": 1680,\n");
        prompt.append("      \"description\": \"빠른 이동 경로\",\n");
        prompt.append("      \"route_steps\": [\n");
        prompt.append("        {\"instruction\": \"구체적인 이동 안내\", \"distance_m\": 200, \"duration_min\": 2}\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    // 공통 Gemini API 호출 메서드
    private String callGeminiAPI(String prompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            
            // Contents 구조
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            
            requestBody.put("contents", List.of(content));
            
            // Generation Config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", temperature);
            generationConfig.put("maxOutputTokens", maxOutputTokens);
            
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.debug("Gemini API 요청: {}", prompt);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );

            return extractContentFromGeminiResponse(response.getBody());

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage());
        }
    }

    // 걸음수 추천 프롬프트 생성
    private String buildStepsPrompt(String gender, Integer age, Integer height, Integer weight) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 GreenMate의 개인 맞춤형 건강 AI입니다. ");
        prompt.append("사용자의 개인정보를 바탕으로 일일 권장 걸음수를 계산해주세요.\n\n");
        
        prompt.append("사용자 정보:\n");
        prompt.append("- 성별: ").append(gender).append("\n");
        prompt.append("- 나이: ").append(age).append("세\n");
        prompt.append("- 키: ").append(height).append("cm\n");
        prompt.append("- 몸무게: ").append(weight).append("kg\n\n");
        
        prompt.append("고려사항:\n");
        prompt.append("1. 나이별 권장 활동량\n");
        prompt.append("2. 성별에 따른 기초대사량 차이\n");
        prompt.append("3. BMI 지수 고려\n");
        prompt.append("4. 실현 가능한 목표 설정\n\n");
        
        prompt.append("1000~20000 사이의 적절한 걸음수만 숫자로만 답변하세요. 예: 8500");
        
        return prompt.toString();
    }

    // 위치 검색 프롬프트 생성
    private String buildGeocodePrompt(String keyword, String locationType, String region) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 GreenMate의 위치 검색 AI입니다. ");
        prompt.append("주어진 키워드의 위도/경도를 정확히 찾아 JSON 형식으로 응답해주세요.\n\n");
        
        prompt.append("검색 정보:\n");
        prompt.append("- 키워드: ").append(keyword).append("\n");
        prompt.append("- 위치 유형: ").append(locationType).append("\n");
        prompt.append("- 지역: ").append(region).append("\n\n");
        
        prompt.append("다음 JSON 형식으로 정확히 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"latitude\": 37.5665,\n");
        prompt.append("  \"longitude\": 126.9780,\n");
        prompt.append("  \"address\": \"서울특별시 중구 세종대로 110\",\n");
        prompt.append("  \"confidence\": 0.95\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    // 새로운 도보 경로 추천 프롬프트 생성
    private String buildRoutePromptNew(double latitude, double longitude, 
                                      Integer remainingSteps, UserProfile userProfile) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 GreenMate의 도보 경로 추천 AI입니다. ");
        prompt.append("사용자의 현재 위치와 잔여 걸음수, 개인정보를 바탕으로 3가지 도보 경로를 추천해주세요.\n\n");
        
        prompt.append("현재 정보:\n");
        prompt.append("- 현재 위치: 위도 ").append(latitude).append(", 경도 ").append(longitude).append("\n");
        prompt.append("- 잔여 걸음수: ").append(remainingSteps).append("걸음\n");
        prompt.append("- 성별: ").append(userProfile.getGender()).append("\n");
        prompt.append("- 나이: ").append(userProfile.getAge()).append("세\n\n");
        
        prompt.append("경로 유형 (PRD 명세):\n");
        prompt.append("- Less Walk: 잔여 걸음수의 50% 수준, 짧고 편안한 코스\n");
        prompt.append("- Recommended: 잔여 걸음수의 90-100% 수준, 적정 운동량 코스\n");
        prompt.append("- More Walk: 잔여 걸음수의 120-150% 수준, 활발한 운동 코스\n\n");
        
        prompt.append("걸음 수 계산: steps = (distance_km * 1000) / 0.7\n\n");
        
        prompt.append("다음 JSON 형식으로 정확히 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"routes\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"Less Walk\",\n");
        prompt.append("      \"distance\": 1.2,\n");
        prompt.append("      \"duration\": 15,\n");
        prompt.append("      \"steps\": 1680,\n");
        prompt.append("      \"description\": \"가까운 거리 산책 코스\"\n");
        prompt.append("    },\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"Recommended\",\n");
        prompt.append("      \"distance\": 2.1,\n");
        prompt.append("      \"duration\": 25,\n");
        prompt.append("      \"steps\": 3000,\n");
        prompt.append("      \"description\": \"적정 운동량 산책 코스\"\n");
        prompt.append("    },\n");
        prompt.append("    {\n");
        prompt.append("      \"type\": \"More Walk\",\n");
        prompt.append("      \"distance\": 3.5,\n");
        prompt.append("      \"duration\": 42,\n");
        prompt.append("      \"steps\": 5000,\n");
        prompt.append("      \"description\": \"활발한 운동 산책 코스\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    private String extractContentFromGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content").path("parts");
                if (content.isArray() && content.size() > 0) {
                    String text = content.get(0).path("text").asText();
                    return cleanJsonResponse(text);
                }
            }
            throw new RuntimeException("Gemini 응답에서 콘텐츠를 찾을 수 없습니다");
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 중 오류 발생: {}", responseBody, e);
            throw new RuntimeException("Gemini 응답을 파싱할 수 없습니다");
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null) return "{}";
        
        // JSON 코드 블록 제거
        String cleaned = response.replaceAll("```json\\s*", "")
                                .replaceAll("```\\s*$", "")
                                .trim();
        
        // JSON 유효성 검사
        try {
            JsonNode jsonNode = objectMapper.readTree(cleaned);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.warn("JSON 파싱 실패, 원본 응답 반환: {}", cleaned);
            return cleaned;
        }
    }
}