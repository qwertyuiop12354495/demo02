package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.activity.common.enums.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_registration")
public class ActivityRegistration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long activityId;

    private RegistrationStatus status;

    private LocalDateTime applyTime;

    private LocalDateTime auditTime;

    private String auditRemark;

    private String applyRemark;

    private Long auditedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
