package com.example.activity.vo.registration;

import com.example.activity.common.enums.RegistrationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationBriefVO {

    private Long id;

    private RegistrationStatus status;

    private LocalDateTime applyTime;
}
