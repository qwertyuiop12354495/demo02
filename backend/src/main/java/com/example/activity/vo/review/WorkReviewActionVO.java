package com.example.activity.vo.review;

import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import lombok.Data;

@Data
public class WorkReviewActionVO {

    private Long workId;

    private WorkStepEnum currentStep;

    private WorkStatusEnum currentStatus;
}
