package com.example.activity.dto.request.notice;

import com.example.activity.common.enums.NoticeTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublicNoticeSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过200字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 2000, message = "异议说明不能超过2000字")
    private String objectionNote;

    @NotNull(message = "公示类型不能为空")
    private NoticeTypeEnum noticeType;

    @NotNull(message = "可见范围类型不能为空")
    private VisibleScopeTypeEnum visibleScopeType;

    @Size(max = 100, message = "省名称不能超过100字")
    private String provinceName;

    @Size(max = 100, message = "市名称不能超过100字")
    private String cityName;

    @Size(max = 100, message = "区县名称不能超过100字")
    private String districtName;

    @Size(max = 200, message = "学校名称不能超过200字")
    private String schoolName;
}
