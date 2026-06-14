package com.example.activity.dto.request.registration;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationCreateRequest {

    @NotNull(message = "活动ID不能为空")
    @Positive(message = "活动ID必须大于0")
    private Long activityId;

    @Size(max = 500, message = "备注不能超过500字符")
    private String remark;
}
