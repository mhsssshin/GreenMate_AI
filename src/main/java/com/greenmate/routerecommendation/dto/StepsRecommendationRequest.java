package com.greenmate.routerecommendation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepsRecommendationRequest {
    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;

    @NotBlank(message = "성별은 필수입니다")
    private String gender; // "male" or "female"

    @NotNull(message = "나이는 필수입니다")
    private Integer age;

    @NotNull(message = "키는 필수입니다")
    private Integer height; // cm

    @NotNull(message = "몸무게는 필수입니다")
    private Integer weight; // kg
}