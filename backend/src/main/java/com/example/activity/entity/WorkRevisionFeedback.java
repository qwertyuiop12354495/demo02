package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.activity.common.enums.WorkStepEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_revision_feedback")
public class WorkRevisionFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workId;

    private WorkStepEnum reviewStep;

    private Integer roundNo;

    private String feedback;

    private Long reviewerId;

    private LocalDateTime createdAt;
}
