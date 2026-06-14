package com.example.activity.dto.request.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationAuditRequest {

    @NotBlank(message = "审核动作不能为空")
    private String action;

    @Size(max = 500, message = "审核备注不能超过500字符")
    private String auditRemark;
}
