package com.greenmate.routerecommendation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeRequest {
    @NotBlank(message = "키워드는 필수입니다")
    private String keyword;

    private String locationType; // "address", "building", "station", "landmark"

    private String region; // 기본값: "대한민국"
}