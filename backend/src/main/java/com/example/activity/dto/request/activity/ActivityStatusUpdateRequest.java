package com.example.activity.dto.request.activity;

import com.example.activity.common.enums.PublishableActivityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityStatusUpdateRequest {

    @NotNull(message = "目标状态不能为空")
    private PublishableActivityStatus status;
}
