package com.example.activity.controller;

import com.example.activity.common.auth.UserContext;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.ActivityPageQuery;
import com.example.activity.service.ActivityService;
import com.example.activity.vo.activity.ActivityVO;
import com.example.activity.vo.activity.UserActivityListItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
@Validated
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public Result<PageResult<UserActivityListItemVO>> pageActivities(@Valid ActivityPageQuery query) {
        return Result.success(activityService.pagePublishedActivities(query, UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<ActivityVO> getActivityDetail(@PathVariable @Positive(message = "活动ID必须大于0") Long id) {
        return Result.success(activityService.getPublishedActivityDetail(id, UserContext.getUserId()));
    }
}
