package com.example.activity.vo.score;

import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScoreWorkListItemVO {

    private Long id;

    private Long activityId;

    private String activityTitle;

    private String title;

    private String category;

    private Long teacherId;

    private WorkStepEnum currentStep;

    private WorkStatusEnum currentStatus;

    private FinalResultEnum finalResult;

    private LocalDateTime updatedAt;
}
