package com.example.activity.dto.request.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkRevisionFeedbackRequest {

    @NotBlank(message = "修改意见不能为空")
    @Size(min = 1, max = 2000, message = "修改意见长度为1-2000字")
    private String feedback;
}
