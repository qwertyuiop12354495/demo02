package com.example.activity.controller.admin;

import com.example.activity.common.auth.RequireAnyRole;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.ScoreWorkPageQuery;
import com.example.activity.dto.request.score.SubmitReviewRequest;
import com.example.activity.service.ScoreService;
import com.example.activity.vo.score.ScopedScorerStatsVO;
import com.example.activity.vo.score.ScoreWorkListItemVO;
import com.example.activity.vo.score.SubmitReviewResultVO;
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
@RequestMapping("/api/admin/scores")
@Validated
@RequiredArgsConstructor
public class AdminScoreController {

    private final ScoreService scoreService;

    @GetMapping("/works")
    @RequireAnyRole({
            RoleTypeEnum.DISTRICT_REVIEWER,
            RoleTypeEnum.CITY_REVIEWER,
            RoleTypeEnum.PROVINCE_REVIEWER
    })
    public Result<PageResult<ScoreWorkListItemVO>> scoreWorks(@Valid ScoreWorkPageQuery query) {
        return Result.success(scoreService.scoreWorks(query));
    }

    @GetMapping("/works/{workId}/scorers")
    @RequireAnyRole({
            RoleTypeEnum.DISTRICT_REVIEWER,
            RoleTypeEnum.CITY_REVIEWER,
            RoleTypeEnum.PROVINCE_REVIEWER
    })
    public Result<ScopedScorerStatsVO> listScopedScorers(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId) {
        return Result.success(scoreService.listScopedScorers(workId));
    }

    @PostMapping("/works/{workId}/review")
    @RequireAnyRole({
            RoleTypeEnum.DISTRICT_REVIEWER,
            RoleTypeEnum.CITY_REVIEWER,
            RoleTypeEnum.PROVINCE_REVIEWER
    })
    public Result<SubmitReviewResultVO> submitReview(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId,
            @Valid @RequestBody SubmitReviewRequest request) {
        return Result.success(scoreService.submitReview(workId, request));
    }
}
