package com.example.activity.converter;

import com.example.activity.entity.PublicNotice;
import com.example.activity.entity.SysUser;
import com.example.activity.mapper.dto.PromotionSummaryRow;
import com.example.activity.vo.notice.PromotionSummaryItemVO;
import com.example.activity.vo.notice.PublicNoticeDetailVO;
import com.example.activity.vo.notice.PublicNoticeListItemVO;
import org.springframework.stereotype.Component;

@Component
public class PublicNoticeConverter {

    public PromotionSummaryItemVO toPromotionItem(PromotionSummaryRow row) {
        PromotionSummaryItemVO vo = new PromotionSummaryItemVO();
        vo.setWorkId(row.getWorkId());
        vo.setWorkTitle(row.getWorkTitle());
        vo.setSchoolName(row.getSchoolName());
        vo.setDistrictName(row.getDistrictName());
        vo.setCityName(row.getCityName());
        vo.setProvinceName(row.getProvinceName());
        vo.setAverageScore(row.getAverageScore());
        vo.setPublishedAt(row.getPublishedAt());
        return vo;
    }

    public PublicNoticeListItemVO toListItemVO(PublicNotice notice, SysUser publisher) {
        PublicNoticeListItemVO vo = new PublicNoticeListItemVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setNoticeType(notice.getNoticeType());
        vo.setVisibleScopeType(notice.getVisibleScopeType());
        vo.setPublishTime(notice.getPublishTime());
        vo.setPublisherName(publisher != null ? publisher.getNickname() : null);
        vo.setPublished(notice.getPublishTime() != null);
        return vo;
    }

    public PublicNoticeDetailVO toDetailVO(PublicNotice notice, SysUser publisher) {
        PublicNoticeDetailVO vo = new PublicNoticeDetailVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setObjectionNote(notice.getObjectionNote());
        vo.setNoticeType(notice.getNoticeType());
        vo.setVisibleScopeType(notice.getVisibleScopeType());
        vo.setProvinceName(notice.getProvinceName());
        vo.setCityName(notice.getCityName());
        vo.setDistrictName(notice.getDistrictName());
        vo.setSchoolName(notice.getSchoolName());
        vo.setCreatedBy(notice.getCreatedBy());
        vo.setPublisherName(publisher != null ? publisher.getNickname() : null);
        vo.setPublishTime(notice.getPublishTime());
        vo.setCreatedAt(notice.getCreatedAt());
        vo.setUpdatedAt(notice.getUpdatedAt());
        return vo;
    }
}
