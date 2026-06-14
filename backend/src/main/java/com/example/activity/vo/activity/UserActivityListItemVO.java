package com.example.activity.vo.activity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserActivityListItemVO {

    private Long id;

    private String title;

    private String location;

    private LocalDateTime eventStartTime;

    private LocalDateTime eventEndTime;

    private LocalDateTime registrationStartTime;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private Integer approvedCount;

    private Integer remainingSlots;

    private Boolean canRegister;

    private String registerDisabledReason;
}
