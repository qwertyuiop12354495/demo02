package com.example.activity.service;

import com.example.activity.dto.request.work.WorkFileRegisterRequest;
import com.example.activity.vo.work.WorkFileVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {

    WorkFileVO uploadFile(Long workId, MultipartFile file);

    WorkFileVO registerFile(Long workId, WorkFileRegisterRequest request);

    void deleteFile(Long workId, Long fileId);

    List<WorkFileVO> listFiles(Long workId);
}
