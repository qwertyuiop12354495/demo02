package com.example.activity.dto.query;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ActivityPageQuery {

    @Min(value = 1, message = "页码不能小于1")
    private long page = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long pageSize = 10;

    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime eventStartFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime eventStartTo;

    @AssertTrue(message = "活动开始筛选时间不能晚于结束时间")
    public boolean isEventTimeRangeValid() {
        if (eventStartFrom == null || eventStartTo == null) {
            return true;
        }
        return !eventStartFrom.isAfter(eventStartTo);
    }
}
