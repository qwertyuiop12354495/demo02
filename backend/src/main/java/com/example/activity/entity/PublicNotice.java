package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.activity.common.enums.NoticeTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import com.example.activity.common.scope.ScopedData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("public_notice")
public class PublicNotice implements ScopedData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String objectionNote;

    private NoticeTypeEnum noticeType;

    private VisibleScopeTypeEnum visibleScopeType;

    private String provinceName;

    private String cityName;

    private String districtName;

    private String schoolName;

    private Long createdBy;

    private LocalDateTime publishTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
