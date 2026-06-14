package com.example.activity.vo.review;

import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkReviewListItemVO {

    private Long id;

    private Long activityId;

    private String activityTitle;

    private String title;

    private String category;

    private Long teacherId;

    private String provinceName;

    private String cityName;

    private String districtName;

    private String schoolName;

    private WorkStepEnum currentStep;

    private WorkStatusEnum currentStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
