package com.example.activity.vo.notice;

import com.example.activity.common.enums.NoticeTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicNoticeListItemVO {

    private Long id;

    private String title;

    private NoticeTypeEnum noticeType;

    private VisibleScopeTypeEnum visibleScopeType;

    private LocalDateTime publishTime;

    private String publisherName;

    private boolean published;
}
