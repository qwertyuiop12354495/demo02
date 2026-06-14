package com.example.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.ReviewResultEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.converter.ScoreConverter;
import com.example.activity.dto.query.ScoreWorkPageQuery;
import com.example.activity.dto.request.score.SubmitReviewRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.ReviewRecord;
import com.example.activity.entity.Work;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.ReviewRecordMapper;
import com.example.activity.mapper.WorkMapper;
import com.example.activity.service.impl.ScoreServiceImpl;
import com.example.activity.service.support.ScoreAccessGuard;
import com.example.activity.service.support.ScorePromotionPolicy;
import com.example.activity.service.support.ScoreScopeQueryBuilder;
import com.example.activity.service.support.ScoreWeightCalculator;
import com.example.activity.service.support.ReviewRecordResultUpdater;
import com.example.activity.service.support.ScopedScorerCounter;
import com.example.activity.vo.score.ScopedScorerStatsVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceImplTest {

    @Mock
    private WorkMapper workMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private ReviewRecordMapper reviewRecordMapper;

    @Spy
    private ScoreConverter scoreConverter = new ScoreConverter();

    @Spy
    private ScoreAccessGuard scoreAccessGuard = new ScoreAccessGuard();

    @Spy
    private ScoreScopeQueryBuilder scoreScopeQueryBuilder = new ScoreScopeQueryBuilder();

    @Spy
    private ScoreWeightCalculator scoreWeightCalculator = new ScoreWeightCalculator();

    @Spy
    private ScorePromotionPolicy scorePromotionPolicy = new ScorePromotionPolicy();

    @Mock
    private ScopedScorerCounter scopedScorerCounter;

    @Mock
    private ReviewRecordResultUpdater reviewRecordResultUpdater;

    @InjectMocks
    private ScoreServiceImpl scoreService;

    private AuthUser districtReviewer;
    private AuthUser provinceReviewer;
    private Activity activity;

    @BeforeEach
    void setUp() {
        districtReviewer = AuthUser.of(20L, "dReviewer", RoleTypeEnum.DISTRICT_REVIEWER,
                "广东省", "深圳市", "南山区", null);
        provinceReviewer = AuthUser.of(30L, "pReviewer", RoleTypeEnum.PROVINCE_REVIEWER,
                "广东省", null, null, null);

        activity = new Activity();
        activity.setId(10L);
        activity.setTitle("测试活动");
        activity.setManualScoreWeight(BigDecimal.ONE);
        activity.setAiScoreWeight(BigDecimal.ZERO);
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void scoreWorks_shouldReturnDistrictSubmittedWorks() {
        AuthContext.set(districtReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_DISTRICT, WorkStatusEnum.SUBMITTED);
        Page<Work> page = new Page<>(1, 10);
        page.setRecords(List.of(work));
        page.setTotal(1);
        when(workMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(activityMapper.selectBatchIds(any())).thenReturn(List.of(activity));

        var result = scoreService.scoreWorks(new ScoreWorkPageQuery());

        assertEquals(1, result.getList().size());
        assertEquals(WorkStepEnum.SCORE_DISTRICT, result.getList().get(0).getCurrentStep());
    }

    @Test
    void submitReview_shouldWaitWhenNotAllScorersCompleted() {
        AuthContext.set(districtReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_DISTRICT, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectByIdForUpdate(100L)).thenReturn(work);
        when(reviewRecordMapper.countByWorkLevelAndReviewer(eq(100L), any(), eq(20L))).thenReturn(0);
        when(activityMapper.selectById(10L)).thenReturn(activity);
        when(reviewRecordMapper.insert(any(ReviewRecord.class))).thenReturn(1);

        ScopedScorerStatsVO stats = new ScopedScorerStatsVO();
        stats.setRequiredCount(2);
        stats.setCompletedCount(1);
        when(scopedScorerCounter.countForWork(work, WorkStepEnum.SCORE_DISTRICT)).thenReturn(stats);
        when(workMapper.updateById(any(Work.class))).thenReturn(1);

        SubmitReviewRequest request = new SubmitReviewRequest();
        request.setManualScore(new BigDecimal("80"));

        var result = scoreService.submitReview(100L, request);

        assertFalse(result.isAllCompleted());
        assertEquals("待其他打分员（1/2）", result.getMessage());
        assertEquals(FinalResultEnum.PENDING, result.getFinalResult());
        verify(scorePromotionPolicy, never()).applyAfterAllScored(any(), any(), any());
    }

    @Test
    void submitReview_shouldPromoteWhenAllDistrictScorersCompleted() {
        AuthContext.set(districtReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_DISTRICT, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectByIdForUpdate(100L)).thenReturn(work);
        when(reviewRecordMapper.countByWorkLevelAndReviewer(eq(100L), any(), eq(20L))).thenReturn(0);
        when(activityMapper.selectById(10L)).thenReturn(activity);
        when(reviewRecordMapper.insert(any(ReviewRecord.class))).thenReturn(1);

        ScopedScorerStatsVO stats = new ScopedScorerStatsVO();
        stats.setRequiredCount(2);
        stats.setCompletedCount(2);
        when(scopedScorerCounter.countForWork(work, WorkStepEnum.SCORE_DISTRICT)).thenReturn(stats);
        when(reviewRecordMapper.listFinalScoresByWorkAndLevel(eq(100L), any()))
                .thenReturn(List.of(new BigDecimal("70"), new BigDecimal("60")));
        when(reviewRecordResultUpdater.resolveLevelResult(eq(WorkStepEnum.SCORE_DISTRICT), any()))
                .thenReturn(ReviewResultEnum.PROMOTED);
        doNothing().when(reviewRecordResultUpdater).updateResults(any(), any(), any());
        when(workMapper.updateById(any(Work.class))).thenReturn(1);

        SubmitReviewRequest request = new SubmitReviewRequest();
        request.setManualScore(new BigDecimal("70"));

        var result = scoreService.submitReview(100L, request);

        assertTrue(result.isAllCompleted());
        assertEquals(WorkStepEnum.SCORE_CITY, result.getCurrentStep());
        assertEquals(FinalResultEnum.PENDING, result.getFinalResult());
    }

    @Test
    void submitReview_shouldEliminateWhenDistrictAverageNotAbove60() {
        AuthContext.set(districtReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_DISTRICT, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectByIdForUpdate(100L)).thenReturn(work);
        when(reviewRecordMapper.countByWorkLevelAndReviewer(eq(100L), any(), eq(20L))).thenReturn(0);
        when(activityMapper.selectById(10L)).thenReturn(activity);
        when(reviewRecordMapper.insert(any(ReviewRecord.class))).thenReturn(1);

        ScopedScorerStatsVO stats = new ScopedScorerStatsVO();
        stats.setRequiredCount(1);
        stats.setCompletedCount(1);
        when(scopedScorerCounter.countForWork(work, WorkStepEnum.SCORE_DISTRICT)).thenReturn(stats);
        when(reviewRecordMapper.listFinalScoresByWorkAndLevel(eq(100L), any()))
                .thenReturn(List.of(new BigDecimal("60")));
        when(reviewRecordResultUpdater.resolveLevelResult(eq(WorkStepEnum.SCORE_DISTRICT), any()))
                .thenReturn(ReviewResultEnum.ELIMINATED);
        doNothing().when(reviewRecordResultUpdater).updateResults(any(), any(), any());
        when(workMapper.updateById(any(Work.class))).thenReturn(1);

        SubmitReviewRequest request = new SubmitReviewRequest();
        request.setManualScore(new BigDecimal("60"));

        var result = scoreService.submitReview(100L, request);

        assertEquals(WorkStepEnum.COMPLETED, result.getCurrentStep());
        assertEquals(FinalResultEnum.ELIMINATED, result.getFinalResult());
    }

    @Test
    void submitReview_provinceAwardShouldSetApproved() {
        AuthContext.set(provinceReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_PROVINCE, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectByIdForUpdate(100L)).thenReturn(work);
        when(reviewRecordMapper.countByWorkLevelAndReviewer(eq(100L), any(), eq(30L))).thenReturn(0);
        when(activityMapper.selectById(10L)).thenReturn(activity);
        when(reviewRecordMapper.insert(any(ReviewRecord.class))).thenReturn(1);

        ScopedScorerStatsVO stats = new ScopedScorerStatsVO();
        stats.setRequiredCount(1);
        stats.setCompletedCount(1);
        when(scopedScorerCounter.countForWork(work, WorkStepEnum.SCORE_PROVINCE)).thenReturn(stats);
        when(reviewRecordMapper.listFinalScoresByWorkAndLevel(eq(100L), any()))
                .thenReturn(List.of(new BigDecimal("95")));
        when(reviewRecordResultUpdater.resolveLevelResult(eq(WorkStepEnum.SCORE_PROVINCE), any()))
                .thenReturn(ReviewResultEnum.AWARD);
        doNothing().when(reviewRecordResultUpdater).updateResults(any(), any(), any());
        when(workMapper.updateById(any(Work.class))).thenReturn(1);

        SubmitReviewRequest request = new SubmitReviewRequest();
        request.setManualScore(new BigDecimal("95"));

        var result = scoreService.submitReview(100L, request);

        assertEquals(WorkStatusEnum.APPROVED, result.getCurrentStatus());
        assertEquals(FinalResultEnum.AWARD, result.getFinalResult());
    }

    @Test
    void submitReview_provinceNotAwardedShouldKeepSubmitted() {
        AuthContext.set(provinceReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_PROVINCE, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectByIdForUpdate(100L)).thenReturn(work);
        when(reviewRecordMapper.countByWorkLevelAndReviewer(eq(100L), any(), eq(30L))).thenReturn(0);
        when(activityMapper.selectById(10L)).thenReturn(activity);
        when(reviewRecordMapper.insert(any(ReviewRecord.class))).thenReturn(1);

        ScopedScorerStatsVO stats = new ScopedScorerStatsVO();
        stats.setRequiredCount(1);
        stats.setCompletedCount(1);
        when(scopedScorerCounter.countForWork(work, WorkStepEnum.SCORE_PROVINCE)).thenReturn(stats);
        when(reviewRecordMapper.listFinalScoresByWorkAndLevel(eq(100L), any()))
                .thenReturn(List.of(new BigDecimal("90")));
        when(reviewRecordResultUpdater.resolveLevelResult(eq(WorkStepEnum.SCORE_PROVINCE), any()))
                .thenReturn(ReviewResultEnum.NOT_AWARDED);
        doNothing().when(reviewRecordResultUpdater).updateResults(any(), any(), any());
        when(workMapper.updateById(any(Work.class))).thenReturn(1);

        SubmitReviewRequest request = new SubmitReviewRequest();
        request.setManualScore(new BigDecimal("90"));

        var result = scoreService.submitReview(100L, request);

        assertEquals(WorkStatusEnum.SUBMITTED, result.getCurrentStatus());
        assertEquals(FinalResultEnum.NOT_AWARDED, result.getFinalResult());
    }

    @Test
    void submitReview_shouldRejectDuplicateScore() {
        AuthContext.set(districtReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_DISTRICT, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectByIdForUpdate(100L)).thenReturn(work);
        when(reviewRecordMapper.countByWorkLevelAndReviewer(eq(100L), any(), eq(20L))).thenReturn(1);

        SubmitReviewRequest request = new SubmitReviewRequest();
        request.setManualScore(new BigDecimal("80"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> scoreService.submitReview(100L, request));
        assertEquals(ErrorCode.SCORE_ALREADY_SUBMITTED.getCode(), ex.getCode());
    }

    @Test
    void submitReview_shouldRejectCrossScopeWork() {
        AuthContext.set(districtReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_DISTRICT, WorkStatusEnum.SUBMITTED);
        work.setDistrictName("福田区");
        when(workMapper.selectByIdForUpdate(100L)).thenReturn(work);

        SubmitReviewRequest request = new SubmitReviewRequest();
        request.setManualScore(new BigDecimal("80"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> scoreService.submitReview(100L, request));
        assertEquals(ErrorCode.SCOPE_ACCESS_DENIED.getCode(), ex.getCode());
    }

    @Test
    void listScopedScorers_shouldDelegateToCounter() {
        AuthContext.set(districtReviewer);
        Work work = buildWork(100L, WorkStepEnum.SCORE_DISTRICT, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectById(100L)).thenReturn(work);

        ScopedScorerStatsVO stats = new ScopedScorerStatsVO();
        stats.setRequiredCount(2);
        stats.setCompletedCount(1);
        when(scopedScorerCounter.countForWork(work, WorkStepEnum.SCORE_DISTRICT)).thenReturn(stats);

        var result = scoreService.listScopedScorers(100L);

        assertEquals(2, result.getRequiredCount());
        assertEquals(1, result.getCompletedCount());
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
