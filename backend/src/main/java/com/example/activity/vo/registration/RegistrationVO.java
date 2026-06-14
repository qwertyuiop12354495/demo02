package com.example.activity.vo.registration;

import com.example.activity.common.enums.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationVO {

    private Long id;

    private Long activityId;

    private String activityTitle;

    private Long userId;

    private RegistrationStatus status;

    private LocalDateTime applyTime;

    private LocalDateTime auditTime;

    private String auditRemark;

    private String remark;

    private Long auditedBy;
}
