package com.example.activity.controller.admin;

import com.example.activity.common.auth.RequireAdmin;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.ManualNoticePageQuery;
import com.example.activity.dto.request.notice.PublicNoticeSaveRequest;
import com.example.activity.service.PublicNoticeService;
import com.example.activity.vo.notice.PublicNoticeDetailVO;
import com.example.activity.vo.notice.PublicNoticeListItemVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notices")
@RequireAdmin
@Validated
@RequiredArgsConstructor
public class AdminPublicNoticeController {

    private final PublicNoticeService publicNoticeService;

    @GetMapping
    public Result<PageResult<PublicNoticeListItemVO>> listAdminNotices(
            @Valid ManualNoticePageQuery query) {
        return Result.success(publicNoticeService.listAdminNotices(query));
    }

    @PostMapping
    public Result<PublicNoticeDetailVO> createNotice(@Valid @RequestBody PublicNoticeSaveRequest request) {
        return Result.success(publicNoticeService.createNotice(request));
    }

    @PutMapping("/{id}")
    public Result<PublicNoticeDetailVO> updateNotice(
            @PathVariable @Positive(message = "公示ID必须大于0") Long id,
            @Valid @RequestBody PublicNoticeSaveRequest request) {
        return Result.success(publicNoticeService.updateNotice(id, request));
    }

    @PostMapping("/{id}/publish")
    public Result<PublicNoticeDetailVO> publishNotice(
            @PathVariable @Positive(message = "公示ID必须大于0") Long id) {
        return Result.success(publicNoticeService.publishNotice(id));
    }
}
