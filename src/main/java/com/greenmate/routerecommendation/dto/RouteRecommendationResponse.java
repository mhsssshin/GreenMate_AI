package com.greenmate.routerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRecommendationResponse {
    private String status;
    private RouteData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteData {
        private List<SimpleRoute> routes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleRoute {
        private String type; // "Less Walk", "Recommended", "More Walk"
        private Double distance; // km
        private Integer duration; // minutes
        private Integer steps;
        private String description;
    }
}