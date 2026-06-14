package com.example.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.NoticeTypeEnum;
import com.example.activity.common.enums.PromotionSummaryTabEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.converter.PublicNoticeConverter;
import com.example.activity.dto.query.ManualNoticePageQuery;
import com.example.activity.dto.query.PromotionSummaryQuery;
import com.example.activity.dto.request.notice.PublicNoticeSaveRequest;
import com.example.activity.entity.PublicNotice;
import com.example.activity.entity.SysUser;
import com.example.activity.mapper.PromotionSummaryMapper;
import com.example.activity.mapper.PublicNoticeMapper;
import com.example.activity.mapper.SysUserMapper;
import com.example.activity.mapper.dto.PromotionSummaryRow;
import com.example.activity.service.impl.PublicNoticeServiceImpl;
import com.example.activity.service.support.NoticeScopeMatcher;
import com.example.activity.service.support.PromotionSummaryScopeBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicNoticeServiceImplTest {

    @Mock
    private PromotionSummaryMapper promotionSummaryMapper;

    @Mock
    private PublicNoticeMapper publicNoticeMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Spy
    private PublicNoticeConverter publicNoticeConverter = new PublicNoticeConverter();

    @Spy
    private PromotionSummaryScopeBuilder promotionSummaryScopeBuilder = new PromotionSummaryScopeBuilder();

    @Spy
    private NoticeScopeMatcher noticeScopeMatcher = new NoticeScopeMatcher();

    @InjectMocks
    private PublicNoticeServiceImpl publicNoticeService;

    private AuthUser districtAdmin;

    @BeforeEach
    void setUp() {
        districtAdmin = AuthUser.of(1L, "admin", RoleTypeEnum.DISTRICT_ADMIN,
                "广东省", "深圳市", "南山区", null);
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void listPromotionSummary_shouldReturnDistrictPromoted() {
        AuthContext.set(districtAdmin);
        PromotionSummaryQuery query = new PromotionSummaryQuery();
        query.setTab(PromotionSummaryTabEnum.DISTRICT_PROMOTED);

        PromotionSummaryRow row = new PromotionSummaryRow();
        row.setWorkId(100L);
        row.setWorkTitle("作品A");
        row.setSchoolName("实验小学");
        row.setDistrictName("南山区");
        row.setCityName("深圳市");
        row.setProvinceName("广东省");
        row.setAverageScore(new BigDecimal("85.5"));
        row.setPublishedAt(LocalDateTime.now());

        when(promotionSummaryMapper.countPromotionSummary(
                eq("SCORE_DISTRICT"), eq("PROMOTED"), eq(null),
                eq("广东省"), eq("深圳市"), eq("南山区"), eq(null))).thenReturn(1L);
        when(promotionSummaryMapper.selectPromotionSummary(
                eq("SCORE_DISTRICT"), eq("PROMOTED"), eq(null),
                eq("广东省"), eq("深圳市"), eq("南山区"), eq(null),
                eq(0L), eq(10L))).thenReturn(List.of(row));

        var result = publicNoticeService.listPromotionSummary(query);

        assertEquals(1, result.getList().size());
        assertEquals("作品A", result.getList().get(0).getWorkTitle());
    }

    @Test
    void createNotice_shouldSaveDraft() {
        AuthContext.set(districtAdmin);
        when(publicNoticeMapper.insert(any(PublicNotice.class))).thenAnswer(inv -> {
            PublicNotice notice = inv.getArgument(0);
            notice.setId(10L);
            return 1;
        });
        when(sysUserMapper.selectById(1L)).thenReturn(new SysUser());

        PublicNoticeSaveRequest request = new PublicNoticeSaveRequest();
        request.setTitle("公示标题");
        request.setContent("公示内容");
        request.setNoticeType(NoticeTypeEnum.SCORE_RESULT);
        request.setVisibleScopeType(VisibleScopeTypeEnum.DISTRICT);
        request.setProvinceName("广东省");
        request.setCityName("深圳市");
        request.setDistrictName("南山区");
        request.setObjectionNote("如有异议请联系");

        var result = publicNoticeService.createNotice(request);

        assertEquals("公示标题", result.getTitle());
        verify(publicNoticeMapper).insert(any(PublicNotice.class));
    }

    @Test
    void publishNotice_shouldSetPublishTime() {
        AuthContext.set(districtAdmin);
        PublicNotice notice = new PublicNotice();
        notice.setId(10L);
        notice.setTitle("公示");
        notice.setContent("内容");
        notice.setNoticeType(NoticeTypeEnum.GENERAL);
        notice.setVisibleScopeType(VisibleScopeTypeEnum.DISTRICT);
        notice.setProvinceName("广东省");
        notice.setCityName("深圳市");
        notice.setDistrictName("南山区");
        notice.setCreatedBy(1L);
        when(publicNoticeMapper.selectById(10L)).thenReturn(notice);
        when(publicNoticeMapper.updateById(any(PublicNotice.class))).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(new SysUser());

        var result = publicNoticeService.publishNotice(10L);

        assertEquals(10L, result.getId());
        verify(publicNoticeMapper).updateById(any(PublicNotice.class));
    }

    @Test
    void getManualNoticeDetail_shouldRejectUnpublished() {
        AuthContext.set(districtAdmin);
        PublicNotice notice = new PublicNotice();
        notice.setId(10L);
        notice.setVisibleScopeType(VisibleScopeTypeEnum.PUBLIC);
        notice.setPublishTime(null);
        when(publicNoticeMapper.selectById(10L)).thenReturn(notice);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> publicNoticeService.getManualNoticeDetail(10L));
        assertEquals(ErrorCode.NOTICE_NOT_PUBLISHED.getCode(), ex.getCode());
    }

    @Test
    void updateNotice_shouldRejectNonCreatorEditingPublicNotice() {
        AuthUser otherDistrictAdmin = AuthUser.of(2L, "other", RoleTypeEnum.DISTRICT_ADMIN,
                "广东省", "深圳市", "福田区", null);
        AuthContext.set(otherDistrictAdmin);

        PublicNotice notice = new PublicNotice();
        notice.setId(10L);
        notice.setTitle("全省公示");
        notice.setContent("内容");
        notice.setNoticeType(NoticeTypeEnum.GENERAL);
        notice.setVisibleScopeType(VisibleScopeTypeEnum.PUBLIC);
        notice.setCreatedBy(99L);
        notice.setPublishTime(null);
        when(publicNoticeMapper.selectById(10L)).thenReturn(notice);

        PublicNoticeSaveRequest request = new PublicNoticeSaveRequest();
        request.setTitle("篡改标题");
        request.setContent("篡改内容");
        request.setNoticeType(NoticeTypeEnum.GENERAL);
        request.setVisibleScopeType(VisibleScopeTypeEnum.PUBLIC);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> publicNoticeService.updateNotice(10L, request));
        assertEquals(ErrorCode.FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    void listManualNotices_shouldReturnPublishedInScope() {
        AuthContext.set(districtAdmin);
        PublicNotice notice = new PublicNotice();
        notice.setId(1L);
        notice.setTitle("公示");
        notice.setNoticeType(NoticeTypeEnum.GENERAL);
        notice.setVisibleScopeType(VisibleScopeTypeEnum.DISTRICT);
        notice.setProvinceName("广东省");
        notice.setCityName("深圳市");
        notice.setDistrictName("南山区");
        notice.setCreatedBy(1L);
        notice.setPublishTime(LocalDateTime.now());

        Page<PublicNotice> page = new Page<>(1, 10);
        page.setRecords(List.of(notice));
        page.setTotal(1);
        when(publicNoticeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(sysUserMapper.selectBatchIds(any())).thenReturn(List.of(new SysUser()));

        var result = publicNoticeService.listManualNotices(new ManualNoticePageQuery());

        assertEquals(1, result.getList().size());
    }
}
