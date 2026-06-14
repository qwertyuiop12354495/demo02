package com.example.activity.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    DUPLICATE_REGISTRATION(40001, "您已报名该活动"),
    REGISTRATION_CLOSED(40002, "活动报名已截止"),
    REGISTRATION_NOT_STARTED(40003, "报名尚未开始"),
    QUOTA_FULL(40004, "活动名额已满"),
    ACTIVITY_OFFLINE(40005, "活动已下架"),
    INVALID_STATUS(40006, "当前状态不允许此操作"),
    VALIDATION_FAILED(40007, "参数校验失败"),
    WORK_ALREADY_SUBMITTED(40008, "您已提交该活动作品，不能重复报名"),
    WORK_NOT_EDITABLE(40009, "当前作品状态不可编辑"),
    UPLOAD_DEADLINE_PASSED(40010, "作品上传已截止"),
    WORK_FILE_REQUIRED(40011, "请至少上传一份作品材料"),
    INVALID_FILE(40012, "文件类型或大小不符合要求"),
    REVIEW_STEP_MISMATCH(40013, "当前审核级别不匹配"),
    WORK_NOT_REVIEWABLE(40014, "作品当前状态不可审核"),
    NO_SCORERS_CONFIGURED(40015, "当前辖区未配置打分员"),
    SCORE_ALREADY_SUBMITTED(40016, "您已对该作品提交过评分"),
    WORK_NOT_SCORABLE(40017, "作品当前状态不可评分"),
    NOTICE_NOT_PUBLISHED(40018, "公示尚未发布"),
    NOTICE_NOT_VISIBLE(40019, "无权查看该公示"),
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    FORBIDDEN(40300, "无权限访问"),
    SCOPE_NOT_CONFIGURED(40301, "账号辖区未配置，无法操作"),
    SCOPE_ACCESS_DENIED(40302, "无权访问该辖区数据"),
    NOT_FOUND(40400, "资源不存在"),
    INTERNAL_ERROR(50000, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;
}
