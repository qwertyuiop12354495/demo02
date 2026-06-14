package com.example.activity.vo.work;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkFileVO {

    private Long id;

    private Long workId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private LocalDateTime createdAt;
}
