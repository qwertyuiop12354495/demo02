package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.ReviewLevelEnum;
import com.example.activity.common.enums.ReviewResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.converter.ScoreConverter;
import com.example.activity.dto.query.ScoreWorkPageQuery;
import com.example.activity.dto.request.score.SubmitReviewRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.ReviewRecord;
import com.example.activity.entity.Work;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.ReviewRecordMapper;
import com.example.activity.mapper.WorkMapper;
import com.example.activity.service.ScoreService;
import com.example.activity.service.support.ScoreAccessGuard;
import com.example.activity.service.support.ScorePromotionPolicy;
import com.example.activity.service.support.ScoreScopeQueryBuilder;
import com.example.activity.service.support.ScoreWeightCalculator;
import com.example.activity.service.support.ReviewRecordResultUpdater;
import com.example.activity.service.support.ScopedScorerCounter;
import com.example.activity.vo.score.ScopedScorerStatsVO;
import com.example.activity.vo.score.ScoreWorkListItemVO;
import com.example.activity.vo.score.SubmitReviewResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private static final Set<WorkStepEnum> SCORE_STEPS = Set.of(
            WorkStepEnum.SCORE_DISTRICT,
            WorkStepEnum.SCORE_CITY,
            WorkStepEnum.SCORE_PROVINCE
    );

    private final WorkMapper workMapper;
    private final ActivityMapper activityMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final ScoreConverter scoreConverter;
    private final ScoreAccessGuard scoreAccessGuard;
    private final ScoreScopeQueryBuilder scoreScopeQueryBuilder;
    private final ScoreWeightCalculator scoreWeightCalculator;
    private final ScopedScorerCounter scopedScorerCounter;
    private final ScorePromotionPolicy scorePromotionPolicy;
    private final ReviewRecordResultUpdater reviewRecordResultUpdater;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ScoreWorkListItemVO> scoreWorks(ScoreWorkPageQuery query) {
        AuthUser user = scoreAccessGuard.requireScorer();
        ScopeNameMatcher.requireScopeConfigured(user);

        WorkStepEnum scoreStep = scoreAccessGuard.resolveScoreStep(user.getRoleType());
        ReviewLevelEnum reviewLevel = ReviewLevelEnum.fromWorkStep(scoreStep);

        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<Work>()
                .eq(Work::getDeleted, 0)
                .eq(Work::getCurrentStep, scoreStep)
                .eq(Work::getCurrentStatus, WorkStatusEnum.SUBMITTED)
                .in(Work::getCurrentStep, SCORE_STEPS)
                .and(w -> w.isNull(Work::getFinalResult)
                        .or()
                        .eq(Work::getFinalResult, FinalResultEnum.PENDING))
                .eq(query.getActivityId() != null, Work::getActivityId, query.getActivityId())
                .apply("NOT EXISTS (SELECT 1 FROM review_record rr WHERE rr.work_id = work.id "
                                + "AND rr.review_level = {0} AND rr.reviewer_id = {1} AND rr.final_score IS NOT NULL)",
                        reviewLevel.getValue(), user.getUserId())
                .orderByDesc(Work::getUpdatedAt);
        scoreScopeQueryBuilder.applyScope(wrapper, user);

        Page<Work> page = workMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, Activity> activityMap = loadActivities(page.getRecords());
        List<ScoreWorkListItemVO> list = page.getRecords().stream()
                .map(work -> scoreConverter.toListItemVO(work, activityMap.get(work.getActivityId())))
                .toList();

        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitReviewResultVO submitReview(Long workId, SubmitReviewRequest request) {
        AuthUser user = scoreAccessGuard.requireScorer();
        ScopeNameMatcher.requireScopeConfigured(user);

        Work work = workMapper.selectByIdForUpdate(workId);
        validateWorkForScoring(work, user);

        WorkStepEnum scoreStep = work.getCurrentStep();
        ReviewLevelEnum reviewLevel = ReviewLevelEnum.fromWorkStep(scoreStep);

        ensureNotScoredYet(workId, reviewLevel, user.getUserId());
        requirePendingFinalResult(work);

        Activity activity = activityMapper.selectById(work.getActivityId());
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "活动不存在");
        }

        BigDecimal recordFinalScore = scoreWeightCalculator.calculate(
                activity, request.getManualScore(), request.getAiScore());

        ReviewRecord record = new ReviewRecord();
        record.setWorkId(workId);
        record.setActivityId(work.getActivityId());
        record.setReviewerId(user.getUserId());
        record.setReviewLevel(reviewLevel);
        record.setManualScore(request.getManualScore());
        record.setAiScore(request.getAiScore() != null ? request.getAiScore() : BigDecimal.ZERO);
        record.setFinalScore(recordFinalScore);
        record.setResult(ReviewResultEnum.PENDING);

        try {
            reviewRecordMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.SCORE_ALREADY_SUBMITTED);
        }

        ScopedScorerStatsVO stats = scopedScorerCounter.countForWork(work, scoreStep);
        int requiredCount = stats.getRequiredCount();
        int completedCount = stats.getCompletedCount();

        if (completedCount < requiredCount) {
            work.setFinalResult(FinalResultEnum.PENDING);
            workMapper.updateById(work);
            String message = String.format("待其他打分员（%d/%d）", completedCount, requiredCount);
            return scoreConverter.toSubmitResult(work, requiredCount, completedCount, false, message);
        }

        List<BigDecimal> scores = reviewRecordMapper.listFinalScoresByWorkAndLevel(
                workId, reviewLevel.getValue());
        BigDecimal average = scorePromotionPolicy.average(scores);
        ReviewResultEnum levelResult = reviewRecordResultUpdater.resolveLevelResult(scoreStep, average);
        scorePromotionPolicy.applyAfterAllScored(work, scoreStep, average);
        reviewRecordResultUpdater.updateResults(workId, reviewLevel, levelResult);
        workMapper.updateById(work);

        String message = "本级打分已完成，流程已更新";
        return scoreConverter.toSubmitResult(work, requiredCount, completedCount, true, message);
    }

    @Override
    @Transactional(readOnly = true)
    public ScopedScorerStatsVO listScopedScorers(Long workId) {
        AuthUser user = scoreAccessGuard.requireScorer();
        ScopeNameMatcher.requireScopeConfigured(user);

        Work work = requireExistingWork(workId);
        ScopeNameMatcher.requireAccess(user, work);
        WorkStepEnum scoreStep = scoreAccessGuard.resolveScoreStep(user.getRoleType());
        scoreAccessGuard.requireStepMatchesRole(user, work);

        return scopedScorerCounter.countForWork(work, scoreStep);
    }

    private void validateWorkForScoring(Work work, AuthUser user) {
        if (work == null || Objects.equals(work.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作品不存在");
        }
        ScopeNameMatcher.requireAccess(user, work);
        scoreAccessGuard.requireScorableStatus(work);
        scoreAccessGuard.requireStepMatchesRole(user, work);
        requirePendingFinalResult(work);
    }

    private Work requireExistingWork(Long workId) {
        Work work = workMapper.selectById(workId);
        if (work == null || Objects.equals(work.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作品不存在");
        }
        return work;
    }

    private void requirePendingFinalResult(Work work) {
        FinalResultEnum finalResult = work.getFinalResult();
        if (finalResult != null && finalResult != FinalResultEnum.PENDING) {
            throw new BusinessException(ErrorCode.WORK_NOT_SCORABLE);
        }
    }

    private void ensureNotScoredYet(Long workId, ReviewLevelEnum reviewLevel, Long reviewerId) {
        int existing = reviewRecordMapper.countByWorkLevelAndReviewer(
                workId, reviewLevel.getValue(), reviewerId);
        if (existing > 0) {
            throw new BusinessException(ErrorCode.SCORE_ALREADY_SUBMITTED);
        }
    }

    private Map<Long, Activity> loadActivities(List<Work> works) {
        Set<Long> activityIds = works.stream()
                .map(Work::getActivityId)
                .collect(Collectors.toSet());
        if (activityIds.isEmpty()) {
            return Map.of();
        }
        return activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity()));
    }
}
