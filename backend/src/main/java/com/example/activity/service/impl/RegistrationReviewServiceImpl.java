package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.ReviewLevelEnum;
import com.example.activity.common.enums.ReviewResultEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.converter.WorkReviewConverter;
import com.example.activity.dto.query.WorkEnrolledPageQuery;
import com.example.activity.dto.query.WorkReviewPageQuery;
import com.example.activity.dto.request.review.WorkRevisionFeedbackRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.ReviewRecord;
import com.example.activity.entity.Work;
import com.example.activity.entity.WorkRevisionFeedback;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.ReviewRecordMapper;
import com.example.activity.mapper.WorkMapper;
import com.example.activity.mapper.WorkRevisionFeedbackMapper;
import com.example.activity.service.RegistrationReviewService;
import com.example.activity.service.support.WorkReviewAccessGuard;
import com.example.activity.service.support.WorkReviewScopeQueryBuilder;
import com.example.activity.vo.review.WorkEnrolledListItemVO;
import com.example.activity.vo.review.WorkReviewActionVO;
import com.example.activity.vo.review.WorkReviewListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationReviewServiceImpl implements RegistrationReviewService {

    private static final Set<WorkStepEnum> REVIEW_STEPS = Set.of(
            WorkStepEnum.SCHOOL,
            WorkStepEnum.DISTRICT,
            WorkStepEnum.CITY,
            WorkStepEnum.PROVINCE
    );

    private final WorkMapper workMapper;
    private final ActivityMapper activityMapper;
    private final WorkRevisionFeedbackMapper workRevisionFeedbackMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final WorkReviewConverter workReviewConverter;
    private final WorkReviewAccessGuard workReviewAccessGuard;
    private final WorkReviewScopeQueryBuilder workReviewScopeQueryBuilder;

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkReviewListItemVO> list(WorkReviewPageQuery query) {
        AuthUser user = workReviewAccessGuard.requireReviewAdmin();
        ScopeNameMatcher.requireScopeConfigured(user);

        WorkStepEnum reviewStep = workReviewAccessGuard.resolveReviewStep(user.getRoleType());
        LambdaQueryWrapper<Work> wrapper = buildReviewListWrapper(query, reviewStep);
        workReviewScopeQueryBuilder.applyScope(wrapper, user);

        Page<Work> page = workMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, Activity> activityMap = loadActivities(page.getRecords());
        List<WorkReviewListItemVO> list = page.getRecords().stream()
                .map(work -> workReviewConverter.toListItemVO(work, activityMap.get(work.getActivityId())))
                .toList();

        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkReviewActionVO approve(Long workId) {
        AuthUser user = workReviewAccessGuard.requireReviewAdmin();
        ScopeNameMatcher.requireScopeConfigured(user);

        Work work = requireWorkForReview(workId, user);
        workReviewAccessGuard.requireReviewableStatus(work);
        workReviewAccessGuard.requireStepMatchesRole(user, work);

        WorkStepEnum approvedStep = work.getCurrentStep();
        WorkStepEnum nextStep = workReviewAccessGuard.resolveNextStepOnApprove(approvedStep);
        work.setCurrentStep(nextStep);
        work.setCurrentStatus(WorkStatusEnum.SUBMITTED);
        workMapper.updateById(work);

        insertApproveRecord(work, user, approvedStep);
        return workReviewConverter.toActionVO(work);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkReviewActionVO submitRevisionFeedback(Long workId, WorkRevisionFeedbackRequest request) {
        AuthUser user = workReviewAccessGuard.requireReviewAdmin();
        ScopeNameMatcher.requireScopeConfigured(user);

        String feedback = normalizeFeedback(request.getFeedback());
        Work work = requireWorkForReview(workId, user);
        workReviewAccessGuard.requireReviewableStatus(work);
        workReviewAccessGuard.requireStepMatchesRole(user, work);

        int nextRound = workRevisionFeedbackMapper.selectMaxRoundNo(
                workId, work.getCurrentStep().getValue()) + 1;

        WorkRevisionFeedback revisionFeedback = new WorkRevisionFeedback();
        revisionFeedback.setWorkId(workId);
        revisionFeedback.setReviewStep(work.getCurrentStep());
        revisionFeedback.setRoundNo(nextRound);
        revisionFeedback.setFeedback(feedback);
        revisionFeedback.setReviewerId(user.getUserId());
        workRevisionFeedbackMapper.insert(revisionFeedback);

        work.setCurrentStatus(WorkStatusEnum.REVISION_REQUIRED);
        workMapper.updateById(work);

        return workReviewConverter.toActionVO(work);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkEnrolledListItemVO> listEnrolled(WorkEnrolledPageQuery query) {
        RoleGuard.requireRole(RoleTypeEnum.PROVINCE_ADMIN);
        AuthUser user = com.example.activity.common.auth.AuthContext.require();
        ScopeNameMatcher.requireScopeConfigured(user);

        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<Work>()
                .eq(Work::getDeleted, 0)
                .eq(Work::getCurrentStatus, WorkStatusEnum.APPROVED)
                .eq(query.getActivityId() != null, Work::getActivityId, query.getActivityId())
                .eq(query.getFinalResult() != null, Work::getFinalResult, query.getFinalResult())
                .orderByDesc(Work::getUpdatedAt);
        workReviewScopeQueryBuilder.applyScope(wrapper, user);

        Page<Work> page = workMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, Activity> activityMap = loadActivities(page.getRecords());
        List<WorkEnrolledListItemVO> list = page.getRecords().stream()
                .map(work -> workReviewConverter.toEnrolledListItemVO(work, activityMap.get(work.getActivityId())))
                .toList();

        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private LambdaQueryWrapper<Work> buildReviewListWrapper(WorkReviewPageQuery query, WorkStepEnum reviewStep) {
        return new LambdaQueryWrapper<Work>()
                .eq(Work::getDeleted, 0)
                .eq(Work::getCurrentStatus, WorkStatusEnum.SUBMITTED)
                .eq(Work::getCurrentStep, reviewStep)
                .in(Work::getCurrentStep, REVIEW_STEPS)
                .eq(query.getActivityId() != null, Work::getActivityId, query.getActivityId())
                .orderByDesc(Work::getUpdatedAt);
    }

    private Work requireWorkForReview(Long workId, AuthUser user) {
        Work work = workMapper.selectById(workId);
        if (work == null || Objects.equals(work.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作品不存在");
        }
        ScopeNameMatcher.requireAccess(user, work);
        return work;
    }

    private void insertApproveRecord(Work work, AuthUser user, WorkStepEnum approvedStep) {
        ReviewRecord record = new ReviewRecord();
        record.setWorkId(work.getId());
        record.setActivityId(work.getActivityId());
        record.setReviewerId(user.getUserId());
        record.setReviewLevel(ReviewLevelEnum.fromWorkStep(approvedStep));
        record.setResult(ReviewResultEnum.APPROVED);

        try {
            reviewRecordMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "该作品在当前级别已审核");
        }
    }

    private String normalizeFeedback(String feedback) {
        if (!StringUtils.hasText(feedback)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "修改意见不能为空");
        }
        String trimmed = feedback.trim();
        if (trimmed.length() > 2000) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "修改意见不能超过2000字");
        }
        return trimmed;
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
