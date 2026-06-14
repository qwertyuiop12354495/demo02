package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.activity.common.enums.ReviewLevelEnum;
import com.example.activity.common.enums.ReviewResultEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review_record")
public class ReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workId;

    private Long activityId;

    private Long reviewerId;

    private ReviewLevelEnum reviewLevel;

    private BigDecimal manualScore;

    private BigDecimal aiScore;

    private BigDecimal finalScore;

    private ReviewResultEnum result;

    private LocalDateTime createdAt;
}
