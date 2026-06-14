package com.example.activity.vo.activity;

import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.vo.registration.RegistrationBriefVO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityVO {

    private Long id;

    private String title;

    private String description;

    private String location;

    private LocalDateTime eventStartTime;

    private LocalDateTime eventEndTime;

    private LocalDateTime registrationStartTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer approvedCount;

    private Integer remainingSlots;

    private ActivityStatus status;

    private Boolean canRegister;

    private String registerDisabledReason;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private RegistrationBriefVO myRegistration;
}
