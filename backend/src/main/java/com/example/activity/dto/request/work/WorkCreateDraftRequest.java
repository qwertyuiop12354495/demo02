package com.example.activity.dto.request.work;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WorkCreateDraftRequest {

    @NotNull(message = "活动ID不能为空")
    @Positive(message = "活动ID必须大于0")
    private Long activityId;
}
