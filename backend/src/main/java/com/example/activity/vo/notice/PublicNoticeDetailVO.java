package com.example.activity.vo.notice;

import com.example.activity.common.enums.NoticeTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicNoticeDetailVO {

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

    private String publisherName;

    private LocalDateTime publishTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
