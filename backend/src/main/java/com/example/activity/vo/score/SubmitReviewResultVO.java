package com.example.activity.vo.score;

import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitReviewResultVO {

    private Long workId;

    private WorkStepEnum currentStep;

    private WorkStatusEnum currentStatus;

    private FinalResultEnum finalResult;

    private BigDecimal finalScore;

    private int requiredCount;

    private int completedCount;

    private boolean allCompleted;

    private String message;
}
