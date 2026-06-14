package com.example.activity.service;

import com.example.activity.common.result.PageResult;
import com.example.activity.dto.query.ManualNoticePageQuery;
import com.example.activity.dto.query.PromotionSummaryQuery;
import com.example.activity.dto.request.notice.PublicNoticeSaveRequest;
import com.example.activity.vo.notice.PromotionSummaryItemVO;
import com.example.activity.vo.notice.PublicNoticeDetailVO;
import com.example.activity.vo.notice.PublicNoticeListItemVO;

public interface PublicNoticeService {

    PageResult<PromotionSummaryItemVO> listPromotionSummary(PromotionSummaryQuery query);

    PageResult<PublicNoticeListItemVO> listManualNotices(ManualNoticePageQuery query);

    PublicNoticeDetailVO getManualNoticeDetail(Long id);

    PublicNoticeDetailVO createNotice(PublicNoticeSaveRequest request);

    PublicNoticeDetailVO updateNotice(Long id, PublicNoticeSaveRequest request);

    PublicNoticeDetailVO publishNotice(Long id);

    PageResult<PublicNoticeListItemVO> listAdminNotices(ManualNoticePageQuery query);
}
