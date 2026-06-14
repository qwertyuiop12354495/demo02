package com.example.activity.dto.query;

import com.example.activity.common.enums.RegistrationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AdminRegistrationPageQuery {

    @Min(value = 1, message = "页码不能小于1")
    private long page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long pageSize = 10;

    private RegistrationStatus status;
}
