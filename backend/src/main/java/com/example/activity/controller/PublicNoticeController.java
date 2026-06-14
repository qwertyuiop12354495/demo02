package com.example.activity.controller;

import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.ManualNoticePageQuery;
import com.example.activity.dto.query.PromotionSummaryQuery;
import com.example.activity.service.PublicNoticeService;
import com.example.activity.vo.notice.PromotionSummaryItemVO;
import com.example.activity.vo.notice.PublicNoticeDetailVO;
import com.example.activity.vo.notice.PublicNoticeListItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
@Validated
@RequiredArgsConstructor
public class PublicNoticeController {

    private final PublicNoticeService publicNoticeService;

    @GetMapping("/promotion-summary")
    public Result<PageResult<PromotionSummaryItemVO>> listPromotionSummary(
            @Valid PromotionSummaryQuery query) {
        return Result.success(publicNoticeService.listPromotionSummary(query));
    }

    @GetMapping("/manual")
    public Result<PageResult<PublicNoticeListItemVO>> listManualNotices(
            @Valid ManualNoticePageQuery query) {
        return Result.success(publicNoticeService.listManualNotices(query));
    }

    @GetMapping("/manual/{id}")
    public Result<PublicNoticeDetailVO> getManualNoticeDetail(
            @PathVariable @Positive(message = "公示ID必须大于0") Long id) {
        return Result.success(publicNoticeService.getManualNoticeDetail(id));
    }
}
