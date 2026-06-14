package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.activity.common.enums.ActivityStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime signupStartTime;

    private LocalDateTime signupEndTime;

    private LocalDateTime uploadDeadline;

    private Integer maxCount;

    private Integer currentCount;

    private ActivityStatus status;

    private BigDecimal manualScoreWeight;

    private BigDecimal aiScoreWeight;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
