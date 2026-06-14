package com.example.activity.dto.request.work;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkSaveRequest {

    @NotBlank(message = "作品标题不能为空")
    @Size(max = 200, message = "作品标题不能超过200字")
    private String title;

    @Size(max = 100, message = "作品类别不能超过100字")
    private String category;

    @Size(max = 200, message = "使用器材不能超过200字")
    private String equipment;

    @NotNull(message = "时长不能为空")
    @Positive(message = "时长必须大于0")
    private Integer duration;
}
