package com.greenmate.routerecommendation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRecommendationRequest {
    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;

    @NotNull(message = "위도는 필수입니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    private Double longitude;

    @NotNull(message = "잔여 걸음수는 필수입니다")
    @Min(value = 0, message = "잔여 걸음수는 0 이상이어야 합니다")
    private Integer remainingSteps;

    @NotNull(message = "사용자 프로필은 필수입니다")
    @Valid
    private UserProfile userProfile;
}