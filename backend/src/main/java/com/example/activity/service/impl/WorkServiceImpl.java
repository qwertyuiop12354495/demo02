package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkCategoryEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.scope.ScopeNameMatcher;
import com.example.activity.converter.WorkConverter;
import com.example.activity.dto.query.WorkMinePageQuery;
import com.example.activity.dto.request.work.WorkCreateDraftRequest;
import com.example.activity.dto.request.work.WorkSaveRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.Work;
import com.example.activity.entity.WorkFile;
import com.example.activity.entity.WorkRevisionFeedback;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.WorkFileMapper;
import com.example.activity.mapper.WorkMapper;
import com.example.activity.mapper.WorkRevisionFeedbackMapper;
import com.example.activity.service.WorkService;
import com.example.activity.service.support.ActivityEnrollmentValidator;
import com.example.activity.service.support.WorkTeacherAccessSupport;
import com.example.activity.vo.work.WorkListItemVO;
import com.example.activity.vo.work.WorkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {

    private static final String DEFAULT_DRAFT_TITLE = "未命名作品";

    private final WorkMapper workMapper;
    private final WorkFileMapper workFileMapper;
    private final WorkRevisionFeedbackMapper workRevisionFeedbackMapper;
    private final ActivityMapper activityMapper;
    private final WorkConverter workConverter;
    private final ActivityEnrollmentValidator activityEnrollmentValidator;
    private final WorkTeacherAccessSupport workTeacherAccessSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkVO createDraft(WorkCreateDraftRequest request) {
        AuthUser user = requireTeacher();
        ScopeNameMatcher.requireScopeConfigured(user);

        Long activityId = request.getActivityId();
        Activity activity = activityMapper.selectById(activityId);
        activityEnrollmentValidator.requirePublishedAndInSignupPeriod(activity, LocalDateTime.now());

        Work existing = findActiveByTeacherAndActivity(user.getUserId(), activityId);
        if (existing != null) {
            return enrichWorkVo(handleExistingWorkOnCreate(existing, activity), activity);
        }

        Work work = buildNewDraft(activityId, user);
        workMapper.insert(work);
        return enrichWorkVo(workConverter.toVO(work, activity, List.of()), activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkVO saveWork(Long workId, WorkSaveRequest request) {
        AuthUser user = requireTeacher();
        Work work = workTeacherAccessSupport.requireOwnedEditableWorkInOpenWindow(workId, user);
        validateSaveRequest(request);

        work.setTitle(request.getTitle().trim());
        work.setCategory(trimToNull(request.getCategory()));
        work.setEquipment(trimToNull(request.getEquipment()));
        work.setDuration(request.getDuration());
        workMapper.updateById(work);

        Activity activity = activityMapper.selectById(work.getActivityId());
        List<WorkFile> files = listActiveFiles(workId);
        return enrichWorkVo(workConverter.toVO(work, activity, files), activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkVO submitWork(Long workId) {
        AuthUser user = requireTeacher();
        Work work = workTeacherAccessSupport.requireOwnedEditableWork(workId, user);
        Activity activity = workTeacherAccessSupport.requireActivityForSubmit(work);

        long fileCount = countActiveFiles(workId);
        if (fileCount < 1) {
            throw new BusinessException(ErrorCode.WORK_FILE_REQUIRED);
        }

        validateWorkReadyForSubmit(work);

        work.setCurrentStatus(WorkStatusEnum.SUBMITTED);
        workMapper.updateById(work);

        List<WorkFile> files = listActiveFiles(workId);
        return enrichWorkVo(workConverter.toVO(work, activity, files), activity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WorkListItemVO> pageMyWorks(WorkMinePageQuery query) {
        AuthUser user = requireTeacher();

        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<Work>()
                .eq(Work::getTeacherId, user.getUserId())
                .eq(Work::getDeleted, 0)
                .eq(query.getActivityId() != null, Work::getActivityId, query.getActivityId())
                .eq(query.getStatus() != null, Work::getCurrentStatus, query.getStatus())
                .orderByDesc(Work::getUpdatedAt);

        Page<Work> page = workMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, Activity> activityMap = loadActivities(page.getRecords());
        List<WorkListItemVO> list = page.getRecords().stream()
                .map(work -> workConverter.toListItemVO(work, activityMap.get(work.getActivityId())))
                .toList();

        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkVO getMyWorkDetail(Long workId) {
        AuthUser user = requireTeacher();
        Work work = workTeacherAccessSupport.requireOwnedWork(workId, user);
        Activity activity = activityMapper.selectById(work.getActivityId());
        List<WorkFile> files = listActiveFiles(workId);
        return enrichWorkVo(workConverter.toVO(work, activity, files), activity);
    }

    private WorkVO handleExistingWorkOnCreate(Work existing, Activity activity) {
        WorkStatusEnum status = existing.getCurrentStatus();
        if (WorkTeacherAccessSupport.isEditableStatus(status)) {
            List<WorkFile> files = listActiveFiles(existing.getId());
            return enrichWorkVo(workConverter.toVO(existing, activity, files), activity);
        }
        if (status == WorkStatusEnum.SUBMITTED || status == WorkStatusEnum.APPROVED) {
            throw new BusinessException(ErrorCode.WORK_ALREADY_SUBMITTED);
        }
        throw new BusinessException(ErrorCode.INVALID_STATUS, "当前作品状态不允许创建报名");
    }

    private WorkVO enrichWorkVo(WorkVO vo, Activity activity) {
        if (vo == null) {
            return null;
        }
        if (activity != null && vo.getActivityTitle() == null) {
            vo.setActivityTitle(activity.getTitle());
        }
        if (vo.getCurrentStatus() == WorkStatusEnum.REVISION_REQUIRED) {
            vo.setLatestRevisionFeedback(loadLatestRevisionFeedback(vo.getId()));
        }
        return vo;
    }

    private String loadLatestRevisionFeedback(Long workId) {
        WorkRevisionFeedback latest = workRevisionFeedbackMapper.selectOne(
                new LambdaQueryWrapper<WorkRevisionFeedback>()
                        .eq(WorkRevisionFeedback::getWorkId, workId)
                        .orderByDesc(WorkRevisionFeedback::getId)
                        .last("LIMIT 1"));
        if (latest == null || !StringUtils.hasText(latest.getFeedback())) {
            return null;
        }
        return latest.getFeedback().trim();
    }

    private Work buildNewDraft(Long activityId, AuthUser user) {
        Work work = new Work();
        work.setActivityId(activityId);
        work.setTeacherId(user.getUserId());
        work.setTitle(DEFAULT_DRAFT_TITLE);
        work.setProvinceName(user.getProvinceName());
        work.setCityName(user.getCityName());
        work.setDistrictName(user.getDistrictName());
        work.setSchoolName(user.getSchoolName());
        work.setCurrentStep(WorkStepEnum.SCHOOL);
        work.setCurrentStatus(WorkStatusEnum.DRAFT);
        work.setFinalResult(FinalResultEnum.PENDING);
        work.setDeleted(0);
        return work;
    }

    private Work findActiveByTeacherAndActivity(Long teacherId, Long activityId) {
        return workMapper.selectOne(new LambdaQueryWrapper<Work>()
                .eq(Work::getTeacherId, teacherId)
                .eq(Work::getActivityId, activityId)
                .eq(Work::getDeleted, 0));
    }

    private AuthUser requireTeacher() {
        RoleGuard.requireRole(RoleTypeEnum.TEACHER);
        return AuthContext.require();
    }

    private void validateSaveRequest(WorkSaveRequest request) {
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "时长必须大于0");
        }
        if (WorkCategoryEnum.isMusic(request.getCategory())
                && !StringUtils.hasText(request.getEquipment())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "音乐类作品必须填写器材");
        }
    }

    private void validateWorkReadyForSubmit(Work work) {
        if (!StringUtils.hasText(work.getTitle())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "作品标题不能为空");
        }
        if (work.getDuration() == null || work.getDuration() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "时长必须大于0");
        }
        if (WorkCategoryEnum.isMusic(work.getCategory()) && !StringUtils.hasText(work.getEquipment())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "音乐类作品必须填写器材");
        }
    }

    private long countActiveFiles(Long workId) {
        return workFileMapper.selectCount(new LambdaQueryWrapper<WorkFile>()
                .eq(WorkFile::getWorkId, workId)
                .eq(WorkFile::getDeleted, 0));
    }

    private List<WorkFile> listActiveFiles(Long workId) {
        return workFileMapper.selectList(new LambdaQueryWrapper<WorkFile>()
                .eq(WorkFile::getWorkId, workId)
                .eq(WorkFile::getDeleted, 0)
                .orderByDesc(WorkFile::getCreatedAt));
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

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
