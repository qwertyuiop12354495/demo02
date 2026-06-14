package com.example.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.auth.RoleGuard;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.storage.MinioStorageService;
import com.example.activity.converter.WorkConverter;
import com.example.activity.dto.request.work.WorkFileRegisterRequest;
import com.example.activity.entity.Work;
import com.example.activity.entity.WorkFile;
import com.example.activity.mapper.WorkFileMapper;
import com.example.activity.mapper.WorkMapper;
import com.example.activity.service.UploadService;
import com.example.activity.service.support.WorkFileAccessGuard;
import com.example.activity.service.support.WorkFileValidator;
import com.example.activity.service.support.WorkTeacherAccessSupport;
import com.example.activity.vo.work.WorkFileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final WorkMapper workMapper;
    private final WorkFileMapper workFileMapper;
    private final WorkConverter workConverter;
    private final WorkFileValidator workFileValidator;
    private final MinioStorageService minioStorageService;
    private final WorkFileAccessGuard workFileAccessGuard;
    private final WorkTeacherAccessSupport workTeacherAccessSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkFileVO uploadFile(Long workId, MultipartFile file) {
        AuthUser user = requireTeacher();
        workTeacherAccessSupport.requireOwnedEditableWorkInOpenWindow(workId, user);

        workFileValidator.validateUpload(file);
        String fileUrl = minioStorageService.uploadWorkFile(workId, file);

        WorkFile workFile = buildWorkFile(
                workId,
                file.getOriginalFilename(),
                fileUrl,
                file.getContentType(),
                file.getSize()
        );
        workFileMapper.insert(workFile);
        return workConverter.toFileVO(workFile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkFileVO registerFile(Long workId, WorkFileRegisterRequest request) {
        AuthUser user = requireTeacher();
        workTeacherAccessSupport.requireOwnedEditableWorkInOpenWindow(workId, user);

        workFileValidator.validateMeta(request.getFileName(), request.getFileType(), request.getFileSize());
        minioStorageService.validateRegisteredUrl(workId, request.getFileUrl());
        String objectKey = minioStorageService.normalizeToObjectKey(request.getFileUrl());

        WorkFile workFile = buildWorkFile(
                workId,
                request.getFileName().trim(),
                objectKey,
                request.getFileType().trim(),
                request.getFileSize()
        );
        workFileMapper.insert(workFile);
        return workConverter.toFileVO(workFile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long workId, Long fileId) {
        AuthUser user = requireTeacher();
        workTeacherAccessSupport.requireOwnedEditableWorkInOpenWindow(workId, user);

        WorkFile workFile = workFileMapper.selectById(fileId);
        if (workFile == null || Objects.equals(workFile.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "材料不存在");
        }
        if (!Objects.equals(workFile.getWorkId(), workId)) {
            throw new BusinessException(ErrorCode.SCOPE_ACCESS_DENIED);
        }

        workFile.setDeleted(1);
        workFileMapper.updateById(workFile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkFileVO> listFiles(Long workId) {
        AuthUser user = AuthContext.require();
        Work work = requireExistingWork(workId);
        workFileAccessGuard.requireListAccess(user, work);

        return workFileMapper.selectList(new LambdaQueryWrapper<WorkFile>()
                        .eq(WorkFile::getWorkId, workId)
                        .eq(WorkFile::getDeleted, 0)
                        .orderByDesc(WorkFile::getCreatedAt))
                .stream()
                .map(workConverter::toFileVO)
                .toList();
    }

    private AuthUser requireTeacher() {
        RoleGuard.requireRole(RoleTypeEnum.TEACHER);
        return AuthContext.require();
    }

    private Work requireExistingWork(Long workId) {
        Work work = workMapper.selectById(workId);
        if (work == null || Objects.equals(work.getDeleted(), 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "作品不存在");
        }
        return work;
    }

    private WorkFile buildWorkFile(Long workId, String fileName, String fileUrl, String fileType, long fileSize) {
        WorkFile workFile = new WorkFile();
        workFile.setWorkId(workId);
        workFile.setFileName(fileName);
        workFile.setFileUrl(fileUrl);
        workFile.setFileType(fileType != null ? fileType : "application/octet-stream");
        workFile.setFileSize(fileSize);
        workFile.setDeleted(0);
        return workFile;
    }
}
