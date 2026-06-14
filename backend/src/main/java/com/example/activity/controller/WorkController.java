package com.example.activity.controller;

import com.example.activity.common.auth.RequireRole;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.result.PageResult;
import com.example.activity.common.result.Result;
import com.example.activity.dto.query.WorkMinePageQuery;
import com.example.activity.dto.request.work.WorkCreateDraftRequest;
import com.example.activity.dto.request.work.WorkSaveRequest;
import com.example.activity.service.WorkService;
import com.example.activity.vo.work.WorkListItemVO;
import com.example.activity.vo.work.WorkVO;
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
@RequestMapping("/api/works")
@Validated
@RequiredArgsConstructor
@RequireRole(RoleTypeEnum.TEACHER)
public class WorkController {

    private final WorkService workService;

    @PostMapping("/draft")
    public Result<WorkVO> createDraft(@Valid @RequestBody WorkCreateDraftRequest request) {
        return Result.success(workService.createDraft(request));
    }

    @PutMapping("/{id}")
    public Result<WorkVO> saveWork(
            @PathVariable @Positive(message = "作品ID必须大于0") Long id,
            @Valid @RequestBody WorkSaveRequest request) {
        return Result.success(workService.saveWork(id, request));
    }

    @PostMapping("/{id}/submit")
    public Result<WorkVO> submitWork(
            @PathVariable @Positive(message = "作品ID必须大于0") Long id) {
        return Result.success(workService.submitWork(id));
    }

    @GetMapping("/mine")
    public Result<PageResult<WorkListItemVO>> pageMyWorks(@Valid WorkMinePageQuery query) {
        return Result.success(workService.pageMyWorks(query));
    }

    @GetMapping("/{id}")
    public Result<WorkVO> getMyWorkDetail(
            @PathVariable @Positive(message = "作品ID必须大于0") Long id) {
        return Result.success(workService.getMyWorkDetail(id));
    }
}
