package com.example.activity.service.support;

import com.example.activity.common.auth.AuthUser;
import com.example.activity.common.enums.NoticeTypeEnum;
import com.example.activity.common.enums.RoleTypeEnum;
import com.example.activity.common.enums.VisibleScopeTypeEnum;
import com.example.activity.entity.PublicNotice;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoticeScopeMatcherTest {

    private final NoticeScopeMatcher matcher = new NoticeScopeMatcher();

    @Test
    void canView_publicNotice_shouldAllowAll() {
        AuthUser teacher = AuthUser.of(1L, "t", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "实验小学");
        PublicNotice notice = publishedNotice(VisibleScopeTypeEnum.PUBLIC, null, null, null, null);
        assertTrue(matcher.canView(teacher, notice));
    }

    @Test
    void canView_districtNotice_shouldMatchDistrictUser() {
        AuthUser teacher = AuthUser.of(1L, "t", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "实验小学");
        PublicNotice notice = publishedNotice(
                VisibleScopeTypeEnum.DISTRICT, "广东省", "深圳市", "南山区", null);
        assertTrue(matcher.canView(teacher, notice));
    }

    @Test
    void canView_districtNotice_shouldRejectOtherDistrict() {
        AuthUser teacher = AuthUser.of(1L, "t", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "福田区", "实验小学");
        PublicNotice notice = publishedNotice(
                VisibleScopeTypeEnum.DISTRICT, "广东省", "深圳市", "南山区", null);
        assertFalse(matcher.canView(teacher, notice));
    }

    @Test
    void canView_unpublished_shouldReject() {
        AuthUser teacher = AuthUser.of(1L, "t", RoleTypeEnum.TEACHER,
                "广东省", "深圳市", "南山区", "实验小学");
        PublicNotice notice = publishedNotice(VisibleScopeTypeEnum.PUBLIC, null, null, null, null);
        notice.setPublishTime(null);
        assertFalse(matcher.canView(teacher, notice));
    }

    private PublicNotice publishedNotice(VisibleScopeTypeEnum scopeType,
                                         String province, String city, String district, String school) {
        PublicNotice notice = new PublicNotice();
        notice.setId(1L);
        notice.setTitle("测试公示");
        notice.setContent("内容");
        notice.setNoticeType(NoticeTypeEnum.GENERAL);
        notice.setVisibleScopeType(scopeType);
        notice.setProvinceName(province);
        notice.setCityName(city);
        notice.setDistrictName(district);
        notice.setSchoolName(school);
        notice.setPublishTime(LocalDateTime.now());
        return notice;
    }
}
