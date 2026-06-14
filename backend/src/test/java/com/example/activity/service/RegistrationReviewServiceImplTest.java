package com.example.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
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
import com.example.activity.service.impl.RegistrationReviewServiceImpl;
import com.example.activity.service.support.WorkReviewAccessGuard;
import com.example.activity.service.support.WorkReviewScopeQueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationReviewServiceImplTest {

    @Mock
    private WorkMapper workMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private WorkRevisionFeedbackMapper workRevisionFeedbackMapper;

    @Mock
    private ReviewRecordMapper reviewRecordMapper;

    @Spy
    private WorkReviewConverter workReviewConverter = new WorkReviewConverter();

    @Spy
    private WorkReviewAccessGuard workReviewAccessGuard = new WorkReviewAccessGuard();

    @Spy
    private WorkReviewScopeQueryBuilder workReviewScopeQueryBuilder = new WorkReviewScopeQueryBuilder();

    @InjectMocks
    private RegistrationReviewServiceImpl registrationReviewService;

    private AuthUser schoolAdmin;
    private AuthUser provinceAdmin;
    private Activity activity;

    @BeforeEach
    void setUp() {
        schoolAdmin = AuthUser.of(1L, "schoolAdmin", RoleTypeEnum.SCHOOL_ADMIN,
                "广东省", "深圳市", "南山区", "实验小学");
        provinceAdmin = AuthUser.of(2L, "provinceAdmin", RoleTypeEnum.PROVINCE_ADMIN,
                "广东省", null, null, null);

        activity = new Activity();
        activity.setId(10L);
        activity.setTitle("测试活动");
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void list_shouldReturnSubmittedWorksAtSchoolStep() {
        AuthContext.set(schoolAdmin);
        Work work = buildWork(100L, WorkStepEnum.SCHOOL, WorkStatusEnum.SUBMITTED);
        Page<Work> page = new Page<>(1, 10);
        page.setRecords(List.of(work));
        page.setTotal(1);
        when(workMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(activityMapper.selectBatchIds(any())).thenReturn(List.of(activity));

        var result = registrationReviewService.list(new WorkReviewPageQuery());

        assertEquals(1, result.getList().size());
        assertEquals(WorkStepEnum.SCHOOL, result.getList().get(0).getCurrentStep());
    }

    @Test
    void approve_shouldAdvanceSchoolToDistrictAndKeepSubmitted() {
        AuthContext.set(schoolAdmin);
        Work work = buildWork(100L, WorkStepEnum.SCHOOL, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectById(100L)).thenReturn(work);
        when(workMapper.updateById(any(Work.class))).thenReturn(1);
        when(reviewRecordMapper.insert(any(ReviewRecord.class))).thenReturn(1);

        var result = registrationReviewService.approve(100L);

        assertEquals(WorkStepEnum.DISTRICT, result.getCurrentStep());
        assertEquals(WorkStatusEnum.SUBMITTED, result.getCurrentStatus());

        ArgumentCaptor<ReviewRecord> captor = ArgumentCaptor.forClass(ReviewRecord.class);
        verify(reviewRecordMapper).insert(captor.capture());
        assertEquals(WorkStepEnum.SCHOOL.name(), captor.getValue().getReviewLevel().name());
    }

    @Test
    void approve_shouldAdvanceProvinceToScoreDistrict() {
        AuthContext.set(provinceAdmin);
        Work work = buildWork(100L, WorkStepEnum.PROVINCE, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectById(100L)).thenReturn(work);
        when(workMapper.updateById(any(Work.class))).thenReturn(1);
        when(reviewRecordMapper.insert(any(ReviewRecord.class))).thenReturn(1);

        var result = registrationReviewService.approve(100L);

        assertEquals(WorkStepEnum.SCORE_DISTRICT, result.getCurrentStep());
        assertEquals(WorkStatusEnum.SUBMITTED, result.getCurrentStatus());
    }

    @Test
    void approve_shouldRejectWhenStepMismatch() {
        AuthContext.set(schoolAdmin);
        Work work = buildWork(100L, WorkStepEnum.DISTRICT, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectById(100L)).thenReturn(work);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationReviewService.approve(100L));
        assertEquals(ErrorCode.REVIEW_STEP_MISMATCH.getCode(), ex.getCode());
        verify(workMapper, never()).updateById(any(Work.class));
    }

    @Test
    void submitRevisionFeedback_shouldSetRevisionRequiredAndKeepStep() {
        AuthContext.set(schoolAdmin);
        Work work = buildWork(100L, WorkStepEnum.SCHOOL, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectById(100L)).thenReturn(work);
        when(workRevisionFeedbackMapper.selectMaxRoundNo(100L, WorkStepEnum.SCHOOL.getValue())).thenReturn(1);
        when(workRevisionFeedbackMapper.insert(any(WorkRevisionFeedback.class))).thenReturn(1);
        when(workMapper.updateById(any(Work.class))).thenReturn(1);

        WorkRevisionFeedbackRequest request = new WorkRevisionFeedbackRequest();
        request.setFeedback("请补充器材说明");

        var result = registrationReviewService.submitRevisionFeedback(100L, request);

        assertEquals(WorkStatusEnum.REVISION_REQUIRED, result.getCurrentStatus());
        assertEquals(WorkStepEnum.SCHOOL, result.getCurrentStep());

        ArgumentCaptor<WorkRevisionFeedback> captor = ArgumentCaptor.forClass(WorkRevisionFeedback.class);
        verify(workRevisionFeedbackMapper).insert(captor.capture());
        assertEquals(2, captor.getValue().getRoundNo());
    }

    @Test
    void submitRevisionFeedback_shouldRejectWhenNotSubmitted() {
        AuthContext.set(schoolAdmin);
        Work work = buildWork(100L, WorkStepEnum.SCHOOL, WorkStatusEnum.DRAFT);
        when(workMapper.selectById(100L)).thenReturn(work);

        WorkRevisionFeedbackRequest request = new WorkRevisionFeedbackRequest();
        request.setFeedback("请修改");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationReviewService.submitRevisionFeedback(100L, request));
        assertEquals(ErrorCode.WORK_NOT_REVIEWABLE.getCode(), ex.getCode());
    }

    @Test
    void listEnrolled_shouldReturnApprovedWorksForProvinceAdmin() {
        AuthContext.set(provinceAdmin);
        Work work = buildWork(100L, WorkStepEnum.COMPLETED, WorkStatusEnum.APPROVED);
        work.setFinalResult(FinalResultEnum.AWARD);
        Page<Work> page = new Page<>(1, 10);
        page.setRecords(List.of(work));
        page.setTotal(1);
        when(workMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(activityMapper.selectBatchIds(any())).thenReturn(List.of(activity));

        var result = registrationReviewService.listEnrolled(new WorkEnrolledPageQuery());

        assertEquals(1, result.getList().size());
        assertEquals(WorkStatusEnum.APPROVED, result.getList().get(0).getCurrentStatus());
    }

    @Test
    void approve_shouldRejectCrossScopeWork() {
        AuthContext.set(schoolAdmin);
        Work work = buildWork(100L, WorkStepEnum.SCHOOL, WorkStatusEnum.SUBMITTED);
        work.setSchoolName("其他学校");
        when(workMapper.selectById(100L)).thenReturn(work);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> registrationReviewService.approve(100L));
        assertEquals(ErrorCode.SCOPE_ACCESS_DENIED.getCode(), ex.getCode());
    }

    private Work buildWork(Long id, WorkStepEnum step, WorkStatusEnum status) {
        Work work = new Work();
        work.setId(id);
        work.setActivityId(10L);
        work.setTeacherId(99L);
        work.setTitle("测试作品");
        work.setProvinceName("广东省");
        work.setCityName("深圳市");
        work.setDistrictName("南山区");
        work.setSchoolName("实验小学");
        work.setCurrentStep(step);
        work.setCurrentStatus(status);
        work.setFinalResult(FinalResultEnum.PENDING);
        work.setDeleted(0);
        return work;
    }
}
