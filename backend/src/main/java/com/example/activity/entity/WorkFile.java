package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("work_file")
public class WorkFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;

    private Integer deleted;

    private LocalDateTime createdAt;
}
