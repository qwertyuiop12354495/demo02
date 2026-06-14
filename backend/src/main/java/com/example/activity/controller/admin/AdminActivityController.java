package com.example.activity.controller.admin;

import com.example.activity.common.auth.RequireRole;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.auth.UserContext;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.AdminActivityPageQuery;
import com.example.activity.dto.query.AdminRegistrationPageQuery;
import com.example.activity.dto.request.activity.ActivityCreateRequest;
import com.example.activity.dto.request.activity.ActivityStatusUpdateRequest;
import com.example.activity.dto.request.activity.ActivityUpdateRequest;
import com.example.activity.service.ActivityService;
import com.example.activity.service.RegistrationService;
import com.example.activity.vo.registration.RegistrationAdminListVO;
import com.example.activity.vo.activity.ActivityStatusUpdateVO;
import com.example.activity.vo.activity.ActivityVO;
import com.example.activity.vo.activity.AdminActivityListItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/activities")
@RequireRole(RoleTypeEnum.PROVINCE_ADMIN)
@Validated
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;
    private final RegistrationService registrationService;

    @GetMapping
    public Result<PageResult<AdminActivityListItemVO>> pageActivities(@Valid AdminActivityPageQuery query) {
        return Result.success(activityService.pageAdminActivities(query));
    }

    @PostMapping
    public Result<ActivityVO> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        return Result.success(activityService.createActivity(request, UserContext.requireUserId()));
    }

    @PutMapping("/{id}")
    public Result<ActivityVO> updateActivity(@PathVariable @Positive(message = "活动ID必须大于0") Long id,
                                             @Valid @RequestBody ActivityUpdateRequest request) {
        return Result.success(activityService.updateActivity(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result<ActivityStatusUpdateVO> updateActivityStatus(
            @PathVariable @Positive(message = "活动ID必须大于0") Long id,
            @Valid @RequestBody ActivityStatusUpdateRequest request) {
        return Result.success(activityService.updateActivityStatus(id, request));
    }

    @GetMapping("/{activityId}/registrations")
    public Result<RegistrationAdminListVO> pageRegistrations(
            @PathVariable @Positive(message = "活动ID必须大于0") Long activityId,
            @Valid AdminRegistrationPageQuery query) {
        return Result.success(registrationService.pageByActivity(activityId, query));
    }
}
