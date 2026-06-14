package com.example.activity.controller.admin;

import com.example.activity.common.auth.RequireAnyRole;
import com.example.activity.common.auth.RequireRole;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.WorkEnrolledPageQuery;
import com.example.activity.dto.query.WorkReviewPageQuery;
import com.example.activity.dto.request.review.WorkRevisionFeedbackRequest;
import com.example.activity.service.RegistrationReviewService;
import com.example.activity.vo.review.WorkEnrolledListItemVO;
import com.example.activity.vo.review.WorkReviewActionVO;
import com.example.activity.vo.review.WorkReviewListItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/work-reviews")
@Validated
@RequiredArgsConstructor
public class AdminWorkReviewController {

    private final RegistrationReviewService registrationReviewService;

    @GetMapping
    @RequireAnyRole({
            RoleTypeEnum.SCHOOL_ADMIN,
            RoleTypeEnum.DISTRICT_ADMIN,
            RoleTypeEnum.CITY_ADMIN,
            RoleTypeEnum.PROVINCE_ADMIN
    })
    public Result<PageResult<WorkReviewListItemVO>> list(@Valid WorkReviewPageQuery query) {
        return Result.success(registrationReviewService.list(query));
    }

    @GetMapping("/enrolled")
    @RequireRole(RoleTypeEnum.PROVINCE_ADMIN)
    public Result<PageResult<WorkEnrolledListItemVO>> listEnrolled(@Valid WorkEnrolledPageQuery query) {
        return Result.success(registrationReviewService.listEnrolled(query));
    }

    @PostMapping("/{workId}/approve")
    @RequireAnyRole({
            RoleTypeEnum.SCHOOL_ADMIN,
            RoleTypeEnum.DISTRICT_ADMIN,
            RoleTypeEnum.CITY_ADMIN,
            RoleTypeEnum.PROVINCE_ADMIN
    })
    public Result<WorkReviewActionVO> approve(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId) {
        return Result.success(registrationReviewService.approve(workId));
    }

    @PostMapping("/{workId}/revision-feedback")
    @RequireAnyRole({
            RoleTypeEnum.SCHOOL_ADMIN,
            RoleTypeEnum.DISTRICT_ADMIN,
            RoleTypeEnum.CITY_ADMIN,
            RoleTypeEnum.PROVINCE_ADMIN
    })
    public Result<WorkReviewActionVO> submitRevisionFeedback(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId,
            @Valid @RequestBody WorkRevisionFeedbackRequest request) {
        return Result.success(registrationReviewService.submitRevisionFeedback(workId, request));
    }
}
