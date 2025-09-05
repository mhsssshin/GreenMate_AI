package com.greenmate.routerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepsRecommendationResponse {
    private String status;
    private StepsData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepsData {
        private Integer recommendedSteps;
    }
}