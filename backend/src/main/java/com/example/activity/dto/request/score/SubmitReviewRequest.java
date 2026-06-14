package com.example.activity.dto.request.score;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitReviewRequest {

    @NotNull(message = "人工分不能为空")
    @DecimalMin(value = "0", message = "人工分不能小于0")
    @DecimalMax(value = "100", message = "人工分不能大于100")
    private BigDecimal manualScore;

    @DecimalMin(value = "0", message = "AI分不能小于0")
    @DecimalMax(value = "100", message = "AI分不能大于100")
    private BigDecimal aiScore;
}
