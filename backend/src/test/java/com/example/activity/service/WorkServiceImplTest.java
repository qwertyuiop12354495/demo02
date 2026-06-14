package com.example.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.storage.WorkFileUrlResolver;
import com.example.activity.converter.WorkConverter;
import com.example.activity.dto.query.WorkMinePageQuery;
import com.example.activity.dto.request.work.WorkCreateDraftRequest;
import com.example.activity.dto.request.work.WorkSaveRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.Work;
import com.example.activity.entity.WorkFile;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.WorkFileMapper;
import com.example.activity.mapper.WorkMapper;
import com.example.activity.mapper.WorkRevisionFeedbackMapper;
import com.example.activity.service.impl.WorkServiceImpl;
import com.example.activity.service.support.ActivityEnrollmentValidator;
import com.example.activity.service.support.WorkTeacherAccessSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkServiceImplTest {

    @Mock
    private WorkMapper workMapper;

    @Mock
    private WorkFileMapper workFileMapper;

    @Mock
    private WorkRevisionFeedbackMapper workRevisionFeedbackMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private WorkFileUrlResolver workFileUrlResolver;

    private WorkConverter workConverter;

    @Spy
    private ActivityEnrollmentValidator activityEnrollmentValidator = new ActivityEnrollmentValidator();

    private WorkTeacherAccessSupport workTeacherAccessSupport;

    private WorkServiceImpl workService;

    private AuthUser teacher;
    private Activity activity;
    private WorkCreateDraftRequest createRequest;

    @BeforeEach
    void setUp() {
        workConverter = org.mockito.Mockito.spy(new WorkConverter(workFileUrlResolver));
        org.mockito.Mockito.lenient()
                .when(workFileUrlResolver.resolveForDownload(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        workTeacherAccessSupport = new WorkTeacherAccessSupport(
                workMapper, activityMapper, activityEnrollmentValidator);
        workService = new WorkServiceImpl(
                workMapper,
                workFileMapper,
                workRevisionFeedbackMapper,
                activityMapper,
                workConverter,
                activityEnrollmentValidator,
                workTeacherAccessSupport);

        teacher = AuthUser.of(10L, "teacher1", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "实验小学");
        AuthContext.set(teacher);

        activity = new Activity();
        activity.setId(1L);
        activity.setTitle("测试活动");
        activity.setStatus(ActivityStatus.PUBLISHED);
        activity.setSignupStartTime(LocalDateTime.now().minusHours(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));

        createRequest = new WorkCreateDraftRequest();
        createRequest.setActivityId(1L);
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void createDraft_shouldCreateNewWork() {
        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(workMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(workMapper.insert(any(Work.class))).thenAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            work.setId(100L);
            return 1;
        });

        var result = workService.createDraft(createRequest);

        assertEquals(100L, result.getId());
        assertEquals(WorkStepEnum.SCHOOL, result.getCurrentStep());
        assertEquals(WorkStatusEnum.DRAFT, result.getCurrentStatus());
        assertEquals("实验小学", result.getSchoolName());

        ArgumentCaptor<Work> captor = ArgumentCaptor.forClass(Work.class);
        verify(workMapper).insert(captor.capture());
        assertEquals(teacher.getUserId(), captor.getValue().getTeacherId());
    }

    @Test
    void createDraft_shouldReuseExistingDraft() {
        Work existing = buildWork(100L, WorkStatusEnum.DRAFT);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(workMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(workFileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        var result = workService.createDraft(createRequest);

        assertEquals(100L, result.getId());
        verify(workMapper, never()).insert(any(Work.class));
    }

    @Test
    void createDraft_shouldRejectWhenAlreadySubmitted() {
        Work existing = buildWork(100L, WorkStatusEnum.SUBMITTED);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(workMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.createDraft(createRequest));
        assertEquals(ErrorCode.WORK_ALREADY_SUBMITTED.getCode(), ex.getCode());
    }

    @Test
    void createDraft_shouldRejectWhenScopeNotConfigured() {
        AuthContext.set(AuthUser.of(10L, "teacher1", RoleTypeEnum.TEACHER, null, null, null, null));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.createDraft(createRequest));
        assertEquals(ErrorCode.SCOPE_NOT_CONFIGURED.getCode(), ex.getCode());
        verify(workMapper, never()).insert(any(Work.class));
    }

    @Test
    void saveWork_shouldRejectMusicWithoutEquipment() {
        Work work = buildWork(100L, WorkStatusEnum.DRAFT);
        when(workMapper.selectById(100L)).thenReturn(work);
        when(activityMapper.selectById(1L)).thenReturn(activity);

        WorkSaveRequest request = new WorkSaveRequest();
        request.setTitle("我的作品");
        request.setCategory("MUSIC");
        request.setDuration(120);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.saveWork(100L, request));
        assertEquals(ErrorCode.VALIDATION_FAILED.getCode(), ex.getCode());
    }

    @Test
    void saveWork_shouldRejectOtherTeacherWork() {
        Work work = buildWork(100L, WorkStatusEnum.DRAFT);
        work.setTeacherId(99L);
        when(workMapper.selectById(100L)).thenReturn(work);

        WorkSaveRequest request = validSaveRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.saveWork(100L, request));
        assertEquals(ErrorCode.SCOPE_ACCESS_DENIED.getCode(), ex.getCode());
    }

    @Test
    void saveWork_shouldRejectWhenSignupClosed() {
        Work work = buildWork(100L, WorkStatusEnum.DRAFT);
        activity.setSignupEndTime(LocalDateTime.now().minusMinutes(1));
        when(workMapper.selectById(100L)).thenReturn(work);
        when(activityMapper.selectById(1L)).thenReturn(activity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.saveWork(100L, validSaveRequest()));
        assertEquals(ErrorCode.REGISTRATION_CLOSED.getCode(), ex.getCode());
    }

    @Test
    void submitWork_shouldChangeStatusToSubmitted() {
        Work work = buildWork(100L, WorkStatusEnum.DRAFT);
        work.setTitle("作品");
        work.setDuration(120);
        when(workMapper.selectById(100L)).thenReturn(work);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(workFileMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(workFileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new WorkFile()));

        var result = workService.submitWork(100L);

        assertEquals(WorkStatusEnum.SUBMITTED, result.getCurrentStatus());
        assertEquals(WorkStepEnum.SCHOOL, result.getCurrentStep());
        verify(workMapper).updateById(any(Work.class));
    }

    @Test
    void submitWork_shouldKeepStepWhenRevisionRequired() {
        Work work = buildWork(100L, WorkStatusEnum.REVISION_REQUIRED);
        work.setCurrentStep(WorkStepEnum.DISTRICT);
        work.setTitle("作品");
        work.setDuration(120);
        when(workMapper.selectById(100L)).thenReturn(work);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(workFileMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(workFileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(new WorkFile()));

        var result = workService.submitWork(100L);

        assertEquals(WorkStatusEnum.SUBMITTED, result.getCurrentStatus());
        assertEquals(WorkStepEnum.DISTRICT, result.getCurrentStep());
    }

    @Test
    void submitWork_shouldRejectWithoutFiles() {
        Work work = buildWork(100L, WorkStatusEnum.DRAFT);
        work.setTitle("作品");
        work.setDuration(120);
        when(workMapper.selectById(100L)).thenReturn(work);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        when(workFileMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.submitWork(100L));
        assertEquals(ErrorCode.WORK_FILE_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void submitWork_shouldRejectWhenSignupClosedForDraft() {
        Work work = buildWork(100L, WorkStatusEnum.DRAFT);
        work.setTitle("作品");
        work.setDuration(120);
        activity.setSignupEndTime(LocalDateTime.now().minusMinutes(1));
        when(workMapper.selectById(100L)).thenReturn(work);
        when(activityMapper.selectById(1L)).thenReturn(activity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.submitWork(100L));
        assertEquals(ErrorCode.REGISTRATION_CLOSED.getCode(), ex.getCode());
    }

    @Test
    void submitWork_shouldRejectWhenUploadDeadlinePassedForRevision() {
        Work work = buildWork(100L, WorkStatusEnum.REVISION_REQUIRED);
        work.setTitle("作品");
        work.setDuration(120);
        activity.setUploadDeadline(LocalDateTime.now().minusMinutes(1));
        when(workMapper.selectById(100L)).thenReturn(work);
        when(activityMapper.selectById(1L)).thenReturn(activity);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workService.submitWork(100L));
        assertEquals(ErrorCode.UPLOAD_DEADLINE_PASSED.getCode(), ex.getCode());
    }

    @Test
    void pageMyWorks_shouldReturnOnlyTeacherWorks() {
        Work work = buildWork(100L, WorkStatusEnum.DRAFT);
        Page<Work> page = new Page<>(1, 10);
        page.setRecords(List.of(work));
        page.setTotal(1);
        when(workMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(activityMapper.selectBatchIds(any())).thenReturn(List.of(activity));

        var result = workService.pageMyWorks(new WorkMinePageQuery());

        assertEquals(1, result.getList().size());
        assertEquals(100L, result.getList().get(0).getId());
    }

    private Work buildWork(Long id, WorkStatusEnum status) {
        Work work = new Work();
        work.setId(id);
        work.setActivityId(1L);
        work.setTeacherId(teacher.getUserId());
        work.setTitle("未命名作品");
        work.setProvinceName(teacher.getProvinceName());
        work.setCityName(teacher.getCityName());
        work.setDistrictName(teacher.getDistrictName());
        work.setSchoolName(teacher.getSchoolName());
        work.setCurrentStep(WorkStepEnum.SCHOOL);
        work.setCurrentStatus(status);
        work.setFinalResult(FinalResultEnum.PENDING);
        work.setDeleted(0);
        return work;
    }

    private WorkSaveRequest validSaveRequest() {
        WorkSaveRequest request = new WorkSaveRequest();
        request.setTitle("我的作品");
        request.setCategory("ART");
        request.setDuration(120);
        return request;
    }
}
