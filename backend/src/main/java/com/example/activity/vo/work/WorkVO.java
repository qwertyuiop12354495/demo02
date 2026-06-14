package com.example.activity.vo.work;

import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkVO {

    private Long id;

    private Long activityId;

    private String activityTitle;

    private Long teacherId;

    private String title;

    private String category;

    private String equipment;

    private Integer duration;

    private String provinceName;

    private String cityName;

    private String districtName;

    private String schoolName;

    private WorkStepEnum currentStep;

    private WorkStatusEnum currentStatus;

    private BigDecimal finalScore;

    private FinalResultEnum finalResult;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<WorkFileVO> files;

    /** 最新退回修改意见（仅 REVISION_REQUIRED 时填充） */
    private String latestRevisionFeedback;
}
