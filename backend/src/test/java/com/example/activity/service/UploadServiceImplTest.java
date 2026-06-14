package com.example.activity.service;

import com.example.activity.common.auth.AuthContext;
import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.ActivityStatus;
import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import com.example.activity.common.storage.MinioStorageService;
import com.example.activity.common.storage.WorkFileUrlResolver;
import com.example.activity.converter.WorkConverter;
import com.example.activity.dto.request.work.WorkFileRegisterRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.Work;
import com.example.activity.entity.WorkFile;
import com.example.activity.mapper.ActivityMapper;
import com.example.activity.mapper.WorkFileMapper;
import com.example.activity.mapper.WorkMapper;
import com.example.activity.service.impl.UploadServiceImpl;
import com.example.activity.service.support.ActivityEnrollmentValidator;
import com.example.activity.service.support.WorkFileAccessGuard;
import com.example.activity.service.support.WorkFileValidator;
import com.example.activity.service.support.WorkTeacherAccessSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

    @Mock
    private WorkMapper workMapper;

    @Mock
    private WorkFileMapper workFileMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private WorkFileUrlResolver workFileUrlResolver;

    private WorkConverter workConverter;

    @Spy
    private WorkFileValidator workFileValidator = new WorkFileValidator();

    @Spy
    private ActivityEnrollmentValidator activityEnrollmentValidator = new ActivityEnrollmentValidator();

    @Mock
    private MinioStorageService minioStorageService;

    @Mock
    private WorkFileAccessGuard workFileAccessGuard;

    private WorkTeacherAccessSupport workTeacherAccessSupport;

    private UploadServiceImpl uploadService;

    private AuthUser teacher;
    private Activity activity;
    private Work editableWork;

    @BeforeEach
    void setUp() {
        workConverter = org.mockito.Mockito.spy(new WorkConverter(workFileUrlResolver));
        org.mockito.Mockito.lenient()
                .when(workFileUrlResolver.resolveForDownload(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        workTeacherAccessSupport = new WorkTeacherAccessSupport(
                workMapper, activityMapper, activityEnrollmentValidator);
        uploadService = new UploadServiceImpl(
                workMapper, workFileMapper, workConverter, workFileValidator,
                minioStorageService, workFileAccessGuard, workTeacherAccessSupport);

        teacher = AuthUser.of(10L, "teacher1", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "实验小学");
        AuthContext.set(teacher);

        activity = new Activity();
        activity.setId(1L);
        activity.setStatus(ActivityStatus.PUBLISHED);
        activity.setSignupStartTime(LocalDateTime.now().minusHours(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));

        editableWork = buildWork(100L, WorkStatusEnum.DRAFT);
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void registerFile_shouldPersistWhenUrlValid() {
        when(workMapper.selectById(100L)).thenReturn(editableWork);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        doNothing().when(minioStorageService).validateRegisteredUrl(eq(100L), any());
        when(minioStorageService.normalizeToObjectKey(any()))
                .thenReturn("works/100/uuid_demo.pdf");
        when(workFileMapper.insert(any(WorkFile.class))).thenAnswer(invocation -> {
            WorkFile file = invocation.getArgument(0);
            file.setId(1L);
            return 1;
        });

        WorkFileRegisterRequest request = new WorkFileRegisterRequest();
        request.setFileName("demo.pdf");
        request.setFileUrl("http://127.0.0.1:9000/activity-works/works/100/uuid_demo.pdf");
        request.setFileType("application/pdf");
        request.setFileSize(1024L);

        var result = uploadService.registerFile(100L, request);

        assertEquals("demo.pdf", result.getFileName());
        verify(workFileMapper).insert(any(WorkFile.class));
    }

    @Test
    void registerFile_shouldRejectInvalidUrlPrefix() {
        when(workMapper.selectById(100L)).thenReturn(editableWork);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        doThrow(new BusinessException(ErrorCode.INVALID_FILE, "文件地址不在允许的存储范围内"))
                .when(minioStorageService).validateRegisteredUrl(eq(100L), any());

        WorkFileRegisterRequest request = new WorkFileRegisterRequest();
        request.setFileName("evil.pdf");
        request.setFileUrl("http://evil.example.com/other/file.pdf");
        request.setFileType("application/pdf");
        request.setFileSize(1024L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploadService.registerFile(100L, request));
        assertEquals(ErrorCode.INVALID_FILE.getCode(), ex.getCode());
    }

    @Test
    void registerFile_shouldRejectOtherTeacherWork() {
        Work otherWork = buildWork(100L, WorkStatusEnum.DRAFT);
        otherWork.setTeacherId(99L);
        when(workMapper.selectById(100L)).thenReturn(otherWork);

        WorkFileRegisterRequest request = new WorkFileRegisterRequest();
        request.setFileName("demo.pdf");
        request.setFileUrl("http://127.0.0.1:9000/activity-works/works/100/uuid_demo.pdf");
        request.setFileType("application/pdf");
        request.setFileSize(1024L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploadService.registerFile(100L, request));
        assertEquals(ErrorCode.SCOPE_ACCESS_DENIED.getCode(), ex.getCode());
    }

    @Test
    void registerFile_shouldRejectWhenSignupClosed() {
        activity.setSignupEndTime(LocalDateTime.now().minusMinutes(1));
        when(workMapper.selectById(100L)).thenReturn(editableWork);
        when(activityMapper.selectById(1L)).thenReturn(activity);

        WorkFileRegisterRequest request = new WorkFileRegisterRequest();
        request.setFileName("demo.pdf");
        request.setFileUrl("http://127.0.0.1:9000/activity-works/works/100/uuid_demo.pdf");
        request.setFileType("application/pdf");
        request.setFileSize(1024L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploadService.registerFile(100L, request));
        assertEquals(ErrorCode.REGISTRATION_CLOSED.getCode(), ex.getCode());
    }

    @Test
    void deleteFile_shouldLogicalDelete() {
        when(workMapper.selectById(100L)).thenReturn(editableWork);
        when(activityMapper.selectById(1L)).thenReturn(activity);
        WorkFile file = new WorkFile();
        file.setId(5L);
        file.setWorkId(100L);
        file.setDeleted(0);
        when(workFileMapper.selectById(5L)).thenReturn(file);

        uploadService.deleteFile(100L, 5L);

        verify(workFileMapper).updateById(any(WorkFile.class));
    }

    @Test
    void uploadFile_shouldRejectWhenWorkSubmitted() {
        Work submitted = buildWork(100L, WorkStatusEnum.SUBMITTED);
        when(workMapper.selectById(100L)).thenReturn(submitted);

        MockMultipartFile file = new MockMultipartFile(
                "file", "demo.pdf", "application/pdf", new byte[]{1, 2, 3});

        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploadService.uploadFile(100L, file));
        assertEquals(ErrorCode.WORK_NOT_EDITABLE.getCode(), ex.getCode());
    }

    private Work buildWork(Long id, WorkStatusEnum status) {
        Work work = new Work();
        work.setId(id);
        work.setActivityId(1L);
        work.setTeacherId(teacher.getUserId());
        work.setTitle("作品");
        work.setProvinceName(teacher.getProvinceName());
        work.setCityName(teacher.getCityName());
        work.setDistrictName(teacher.getDistrictName());
        work.setSchoolName(teacher.getSchoolName());
        work.setCurrentStep(WorkStepEnum.SCHOOL);
        work.setCurrentStatus(status);
        work.setFinalResult(FinalResultEnum.PENDING);
        work.setDeleted(0);
        return work;
    }
}
