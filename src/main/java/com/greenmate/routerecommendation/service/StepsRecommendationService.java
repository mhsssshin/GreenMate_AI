package com.greenmate.routerecommendation.service;

import com.greenmate.routerecommendation.dto.StepsRecommendationRequest;
import com.greenmate.routerecommendation.dto.StepsRecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StepsRecommendationService {

    private final OpenAIService openAIService;

    public StepsRecommendationResponse recommendSteps(StepsRecommendationRequest request) {
        try {
            log.info("걸음수 추천 요청 처리 시작 - 사용자: {}, 성별: {}, 나이: {}", 
                    request.getUserId(), request.getGender(), request.getAge());

            // OpenAI API를 통해 개인 맞춤형 걸음수 추천 받기
            String aiResponse = openAIService.generateStepsRecommendation(
                request.getGender(),
                request.getAge(),
                request.getHeight(),
                request.getWeight()
            );

            // AI 응답에서 걸음수 추출
            Integer recommendedSteps = parseStepsFromAIResponse(aiResponse);

            // 응답 객체 생성
            StepsRecommendationResponse.StepsData stepsData = 
                new StepsRecommendationResponse.StepsData(recommendedSteps);

            StepsRecommendationResponse response = new StepsRecommendationResponse(
                "success", 
                stepsData
            );

            log.info("걸음수 추천 완료 - 추천 걸음수: {}", recommendedSteps);
            return response;

        } catch (Exception e) {
            log.error("걸음수 추천 중 오류 발생", e);
            throw new RuntimeException("걸음수 추천 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Integer parseStepsFromAIResponse(String aiResponse) {
        try {
            // AI 응답에서 숫자 추출 (간단한 파싱)
            String cleanedResponse = aiResponse.replaceAll("[^0-9]", "");
            if (cleanedResponse.isEmpty()) {
                throw new RuntimeException("AI 응답에서 걸음수를 찾을 수 없습니다");
            }
            
            int steps = Integer.parseInt(cleanedResponse);
            
            // 합리적인 범위 검증 (1000 ~ 20000 걸음)
            if (steps < 1000 || steps > 20000) {
                log.warn("AI가 제안한 걸음수가 비정상적입니다: {}. 기본값 적용", steps);
                return 8000; // 기본값
            }
            
            return steps;
            
        } catch (Exception e) {
            log.error("AI 응답에서 걸음수 파싱 중 오류 발생: {}", aiResponse, e);
            // 기본값 반환
            return 8000;
        }
    }
}