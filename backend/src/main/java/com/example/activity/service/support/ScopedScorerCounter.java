package com.example.activity.service.support;

import com.example.activity.common.enums.ReviewLevelEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.entity.Work;
import com.example.activity.mapper.ReviewRecordMapper;
import com.example.activity.mapper.SysUserMapper;
import com.example.activity.vo.score.ScopedScorerStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ScopedScorerCounter {

    private final SysUserMapper sysUserMapper;
    private final ReviewRecordMapper reviewRecordMapper;

    public ScopedScorerStatsVO countForWork(Work work, WorkStepEnum scoreStep) {
        RoleTypeEnum scorerRole = resolveScorerRole(scoreStep);
        ReviewLevelEnum reviewLevel = ReviewLevelEnum.fromWorkStep(scoreStep);

        int requiredCount = countRequiredScorers(work, scorerRole);
        if (requiredCount <= 0) {
            throw new BusinessException(ErrorCode.NO_SCORERS_CONFIGURED);
        }

        int completedCount = reviewRecordMapper.countScoredByWorkAndLevel(
                work.getId(), reviewLevel.getValue());

        ScopedScorerStatsVO stats = new ScopedScorerStatsVO();
        stats.setReviewLevel(reviewLevel);
        stats.setRequiredCount(requiredCount);
        stats.setCompletedCount(completedCount);
        return stats;
    }

    public int countRequiredScorers(Work work, RoleTypeEnum scorerRole) {
        return switch (scorerRole) {
            case DISTRICT_REVIEWER -> {
                requireText(work.getProvinceName(), "省");
                requireText(work.getCityName(), "市");
                requireText(work.getDistrictName(), "区/县");
                yield sysUserMapper.countEnabledScorersByScope(
                        scorerRole.getValue(),
                        work.getProvinceName().trim(),
                        work.getCityName().trim(),
                        work.getDistrictName().trim());
            }
            case CITY_REVIEWER -> {
                requireText(work.getProvinceName(), "省");
                requireText(work.getCityName(), "市");
                yield sysUserMapper.countEnabledScorersByScope(
                        scorerRole.getValue(),
                        work.getProvinceName().trim(),
                        work.getCityName().trim(),
                        null);
            }
            case PROVINCE_REVIEWER -> {
                requireText(work.getProvinceName(), "省");
                yield sysUserMapper.countEnabledScorersByScope(
                        scorerRole.getValue(),
                        work.getProvinceName().trim(),
                        null,
                        null);
            }
            default -> throw new BusinessException(ErrorCode.FORBIDDEN);
        };
    }

    private RoleTypeEnum resolveScorerRole(WorkStepEnum scoreStep) {
        return switch (scoreStep) {
            case SCORE_DISTRICT -> RoleTypeEnum.DISTRICT_REVIEWER;
            case SCORE_CITY -> RoleTypeEnum.CITY_REVIEWER;
            case SCORE_PROVINCE -> RoleTypeEnum.PROVINCE_REVIEWER;
            default -> throw new BusinessException(ErrorCode.WORK_NOT_SCORABLE);
        };
    }

    private void requireText(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "作品" + label + "信息缺失");
        }
    }
}
