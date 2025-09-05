package com.greenmate.routerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeResponse {
    private String status;
    private GeocodeData data;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeocodeData {
        private Double latitude;
        private Double longitude;
        private String address;
        private Double confidence; // 0.0 - 1.0
    }
}