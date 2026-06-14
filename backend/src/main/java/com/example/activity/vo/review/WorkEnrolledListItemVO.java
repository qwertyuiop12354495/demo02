package com.example.activity.vo.review;

import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WorkEnrolledListItemVO {

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

    private FinalResultEnum finalResult;

    private BigDecimal finalScore;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
