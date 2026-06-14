package com.example.activity.vo.activity;

import com.example.activity.common.enums.ActivityStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityStatusUpdateVO {

    private Long id;

    private String title;

    private ActivityStatus status;

    private LocalDateTime updatedAt;
}
