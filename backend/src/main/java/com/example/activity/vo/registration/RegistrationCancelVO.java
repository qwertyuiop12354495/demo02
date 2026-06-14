package com.example.activity.vo.registration;

import com.example.activity.common.enums.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationCancelVO {

    private Long id;

    private Long activityId;

    private RegistrationStatus status;

    private LocalDateTime applyTime;

    private LocalDateTime auditTime;

    private String auditRemark;
}
