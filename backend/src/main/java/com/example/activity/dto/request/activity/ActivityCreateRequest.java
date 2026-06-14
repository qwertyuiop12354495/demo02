package com.example.activity.dto.request.activity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityCreateRequest {

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 200, message = "活动标题不能超过200字符")
    private String title;

    private String description;

    @Size(max = 200, message = "地点不能超过200字符")
    private String location;

    private LocalDateTime eventStartTime;

    private LocalDateTime eventEndTime;

    @NotNull(message = "报名开始时间不能为空")
    private LocalDateTime registrationStartTime;

    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime registrationDeadline;

    @NotNull(message = "人数上限不能为空")
    @Min(value = 1, message = "人数上限不能小于1")
    private Integer maxParticipants;
}
