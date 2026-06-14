package com.example.activity.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ScoreWorkPageQuery {

    @Min(value = 1, message = "页码不能小于1")
    private long page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long pageSize = 10;

    @Positive(message = "活动ID必须大于0")
    private Long activityId;
}
