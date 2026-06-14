package com.example.activity.vo.registration;

import com.example.activity.common.enums.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationListItemVO {

    private Long id;

    private Long activityId;

    private String activityTitle;

    private String activityLocation;

    private LocalDateTime eventStartTime;

    private Long userId;

    private String username;

    private String nickname;

    private RegistrationStatus status;

    private LocalDateTime applyTime;

    private LocalDateTime auditTime;

    private String auditRemark;

    private String remark;

    private Long auditedBy;
}
