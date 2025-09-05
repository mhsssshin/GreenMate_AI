package com.greenmate.routerecommendation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @NotBlank(message = "성별은 필수입니다")
    private String gender; // "male" or "female"

    @NotNull(message = "나이는 필수입니다")
    @Min(value = 1, message = "나이는 1세 이상이어야 합니다")
    private Integer age;

    @NotNull(message = "키는 필수입니다")
    @Min(value = 50, message = "키는 50cm 이상이어야 합니다")
    private Integer height; // cm

    @NotNull(message = "몸무게는 필수입니다")
    @Min(value = 10, message = "몸무게는 10kg 이상이어야 합니다")
    private Integer weight; // kg
}