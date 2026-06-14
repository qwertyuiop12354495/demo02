package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.PromotionSummaryTabEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.result.PageResult;
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
import com.example.activity.service.PublicNoticeService;
import com.example.activity.service.support.NoticeScopeMatcher;
import com.example.activity.service.support.PromotionSummaryScopeBuilder;
import com.example.activity.vo.notice.PromotionSummaryItemVO;
import com.example.activity.vo.notice.PublicNoticeDetailVO;
import com.example.activity.vo.notice.PublicNoticeListItemVO;
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
public class PublicNoticeServiceImpl implements PublicNoticeService {

    private final PromotionSummaryMapper promotionSummaryMapper;
    private final PublicNoticeMapper publicNoticeMapper;
    private final SysUserMapper sysUserMapper;
    private final PublicNoticeConverter publicNoticeConverter;
    private final PromotionSummaryScopeBuilder promotionSummaryScopeBuilder;
    private final NoticeScopeMatcher noticeScopeMatcher;

    @Override
    @Transactional(readOnly = true)
    public PageResult<PromotionSummaryItemVO> listPromotionSummary(PromotionSummaryQuery query) {
        AuthUser user = AuthContext.require();
        PromotionSummaryTabEnum tab = query.getTab();
        PromotionSummaryScopeBuilder.ScopeParams scope = promotionSummaryScopeBuilder.buildWorkScopeFilter(user);

        long offset = (query.getPage() - 1) * query.getPageSize();
        long total = promotionSummaryMapper.countPromotionSummary(
                tab.getReviewLevel().getValue(),
                tab.getResult().getValue(),
                query.getActivityId(),
                scope.getProvinceName(),
                scope.getCityName(),
                scope.getDistrictName(),
                scope.getSchoolName());

        List<PromotionSummaryRow> rows = promotionSummaryMapper.selectPromotionSummary(
                tab.getReviewLevel().getValue(),
                tab.getResult().getValue(),
                query.getActivityId(),
                scope.getProvinceName(),
                scope.getCityName(),
                scope.getDistrictName(),
                scope.getSchoolName(),
                offset,
                query.getPageSize());

        List<PromotionSummaryItemVO> list = rows.stream()
                .map(publicNoticeConverter::toPromotionItem)
                .toList();

        return new PageResult<>(list, total, query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PublicNoticeListItemVO> listManualNotices(ManualNoticePageQuery query) {
        AuthUser user = AuthContext.require();
        LambdaQueryWrapper<PublicNotice> wrapper = buildPublishedVisibilityWrapper(user);
        wrapper.orderByDesc(PublicNotice::getPublishTime);

        Page<PublicNotice> page = publicNoticeMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, SysUser> publisherMap = loadPublishers(page.getRecords());
        List<PublicNoticeListItemVO> list = page.getRecords().stream()
                .filter(notice -> noticeScopeMatcher.canView(user, notice))
                .map(notice -> publicNoticeConverter.toListItemVO(
                        notice, publisherMap.get(notice.getCreatedBy())))
                .toList();

        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicNoticeDetailVO getManualNoticeDetail(Long id) {
        AuthUser user = AuthContext.require();
        PublicNotice notice = requireNotice(id);
        if (notice.getPublishTime() == null) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_PUBLISHED);
        }
        noticeScopeMatcher.requireView(user, notice);
        SysUser publisher = sysUserMapper.selectById(notice.getCreatedBy());
        return publicNoticeConverter.toDetailVO(notice, publisher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicNoticeDetailVO createNotice(PublicNoticeSaveRequest request) {
        AuthUser user = requireAdmin();
        validateSaveRequest(request);

        PublicNotice notice = buildNoticeFromRequest(new PublicNotice(), request);
        notice.setCreatedBy(user.getUserId());
        notice.setPublishTime(null);
        publicNoticeMapper.insert(notice);

        return publicNoticeConverter.toDetailVO(notice, sysUserMapper.selectById(user.getUserId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicNoticeDetailVO updateNotice(Long id, PublicNoticeSaveRequest request) {
        AuthUser user = requireAdmin();
        validateSaveRequest(request);

        PublicNotice notice = requireNotice(id);
        requireDraft(notice);
        requireCreatorOrScopeAdmin(user, notice);

        applySaveRequest(notice, request);
        publicNoticeMapper.updateById(notice);

        return publicNoticeConverter.toDetailVO(notice, sysUserMapper.selectById(notice.getCreatedBy()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublicNoticeDetailVO publishNotice(Long id) {
        AuthUser user = requireAdmin();
        PublicNotice notice = requireNotice(id);
        requireDraft(notice);
        requireCreatorOrScopeAdmin(user, notice);

        notice.setPublishTime(LocalDateTime.now());
        publicNoticeMapper.updateById(notice);

        return publicNoticeConverter.toDetailVO(notice, sysUserMapper.selectById(notice.getCreatedBy()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PublicNoticeListItemVO> listAdminNotices(ManualNoticePageQuery query) {
        AuthUser user = requireAdmin();
        LambdaQueryWrapper<PublicNotice> wrapper = buildAdminVisibilityWrapper(user);
        wrapper.orderByDesc(PublicNotice::getUpdatedAt);

        Page<PublicNotice> page = publicNoticeMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        Map<Long, SysUser> publisherMap = loadPublishers(page.getRecords());
        List<PublicNoticeListItemVO> list = page.getRecords().stream()
                .map(notice -> publicNoticeConverter.toListItemVO(
                        notice, publisherMap.get(notice.getCreatedBy())))
                .toList();

        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private LambdaQueryWrapper<PublicNotice> buildPublishedVisibilityWrapper(AuthUser user) {
        LambdaQueryWrapper<PublicNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(PublicNotice::getPublishTime);
        applyVisibilityOr(wrapper, user);
        return wrapper;
    }

    private LambdaQueryWrapper<PublicNotice> buildAdminVisibilityWrapper(AuthUser user) {
        LambdaQueryWrapper<PublicNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.isNotNull(PublicNotice::getPublishTime)
                .or()
                .isNull(PublicNotice::getPublishTime));
        wrapper.and(w -> w.eq(PublicNotice::getCreatedBy, user.getUserId())
                .or(sub -> applyVisibilityOr(sub, user)));
        return wrapper;
    }

    private void applyVisibilityOr(LambdaQueryWrapper<PublicNotice> wrapper, AuthUser user) {
        wrapper.and(w -> {
            w.eq(PublicNotice::getVisibleScopeType, VisibleScopeTypeEnum.PUBLIC);
            if (StringUtils.hasText(user.getProvinceName())) {
                w.or(o -> o.eq(PublicNotice::getVisibleScopeType, VisibleScopeTypeEnum.PROVINCE)
                        .eq(PublicNotice::getProvinceName, user.getProvinceName().trim()));
                if (StringUtils.hasText(user.getCityName())) {
                    w.or(o -> o.eq(PublicNotice::getVisibleScopeType, VisibleScopeTypeEnum.CITY)
                            .eq(PublicNotice::getProvinceName, user.getProvinceName().trim())
                            .eq(PublicNotice::getCityName, user.getCityName().trim()));
                    if (StringUtils.hasText(user.getDistrictName())) {
                        w.or(o -> o.eq(PublicNotice::getVisibleScopeType, VisibleScopeTypeEnum.DISTRICT)
                                .eq(PublicNotice::getProvinceName, user.getProvinceName().trim())
                                .eq(PublicNotice::getCityName, user.getCityName().trim())
                                .eq(PublicNotice::getDistrictName, user.getDistrictName().trim()));
                        if (StringUtils.hasText(user.getSchoolName())) {
                            w.or(o -> o.eq(PublicNotice::getVisibleScopeType, VisibleScopeTypeEnum.SCHOOL)
                                    .eq(PublicNotice::getProvinceName, user.getProvinceName().trim())
                                    .eq(PublicNotice::getCityName, user.getCityName().trim())
                                    .eq(PublicNotice::getDistrictName, user.getDistrictName().trim())
                                    .eq(PublicNotice::getSchoolName, user.getSchoolName().trim()));
                        }
                    }
                }
            }
        });
    }

    private AuthUser requireAdmin() {
        RoleGuard.requireLegacyAdmin();
        return AuthContext.require();
    }

    private PublicNotice requireNotice(Long id) {
        PublicNotice notice = publicNoticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公示不存在");
        }
        return notice;
    }

    private void requireDraft(PublicNotice notice) {
        if (notice.getPublishTime() != null) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "已发布公示不可编辑");
        }
    }

    private void requireCreatorOrScopeAdmin(AuthUser user, PublicNotice notice) {
        if (Objects.equals(notice.getCreatedBy(), user.getUserId())) {
            return;
        }
        if (notice.getVisibleScopeType() == VisibleScopeTypeEnum.PUBLIC) {
            if (user.getRoleType() == RoleTypeEnum.PROVINCE_ADMIN) {
                return;
            }
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (noticeScopeMatcher.matchesWorkScope(user, notice)) {
            return;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    private void validateSaveRequest(PublicNoticeSaveRequest request) {
        noticeScopeMatcher.validateScopeFields(
                request.getVisibleScopeType(),
                request.getProvinceName(),
                request.getCityName(),
                request.getDistrictName(),
                request.getSchoolName());
    }

    private PublicNotice buildNoticeFromRequest(PublicNotice notice, PublicNoticeSaveRequest request) {
        applySaveRequest(notice, request);
        return notice;
    }

    private void applySaveRequest(PublicNotice notice, PublicNoticeSaveRequest request) {
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setObjectionNote(trimToNull(request.getObjectionNote()));
        notice.setNoticeType(request.getNoticeType());
        notice.setVisibleScopeType(request.getVisibleScopeType());
        notice.setProvinceName(trimToNull(request.getProvinceName()));
        notice.setCityName(trimToNull(request.getCityName()));
        notice.setDistrictName(trimToNull(request.getDistrictName()));
        notice.setSchoolName(trimToNull(request.getSchoolName()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Map<Long, SysUser> loadPublishers(List<PublicNotice> notices) {
        Set<Long> userIds = notices.stream()
                .map(PublicNotice::getCreatedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }
}
