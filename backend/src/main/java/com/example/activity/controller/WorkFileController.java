package com.example.activity.controller;

import com.example.activity.common.auth.RequireAnyRole;
import com.example.activity.common.auth.RequireRole;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.result.Result;
import com.example.activity.dto.request.work.WorkFileRegisterRequest;
import com.example.activity.service.UploadService;
import com.example.activity.vo.work.WorkFileVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/works/{workId}/files")
@Validated
@RequiredArgsConstructor
public class WorkFileController {

    private final UploadService uploadService;

    @PostMapping("/upload")
    @RequireRole(RoleTypeEnum.TEACHER)
    public Result<WorkFileVO> uploadFile(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId,
            @RequestPart("file") MultipartFile file) {
        return Result.success(uploadService.uploadFile(workId, file));
    }

    @PostMapping("/register")
    @RequireRole(RoleTypeEnum.TEACHER)
    public Result<WorkFileVO> registerFile(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId,
            @Valid @RequestBody WorkFileRegisterRequest request) {
        return Result.success(uploadService.registerFile(workId, request));
    }

    @DeleteMapping("/{fileId}")
    @RequireRole(RoleTypeEnum.TEACHER)
    public Result<Void> deleteFile(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId,
            @PathVariable @Positive(message = "材料ID必须大于0") Long fileId) {
        uploadService.deleteFile(workId, fileId);
        return Result.success();
    }

    @GetMapping
    @RequireAnyRole({
            RoleTypeEnum.TEACHER,
            RoleTypeEnum.SCHOOL_ADMIN,
            RoleTypeEnum.DISTRICT_ADMIN,
            RoleTypeEnum.CITY_ADMIN,
            RoleTypeEnum.PROVINCE_ADMIN,
            RoleTypeEnum.DISTRICT_REVIEWER,
            RoleTypeEnum.CITY_REVIEWER,
            RoleTypeEnum.PROVINCE_REVIEWER
    })
    public Result<List<WorkFileVO>> listFiles(
            @PathVariable @Positive(message = "作品ID必须大于0") Long workId) {
        return Result.success(uploadService.listFiles(workId));
    }
}
