package com.example.activity.converter;

import com.example.activity.entity.Activity;
import com.example.activity.entity.Work;
import com.example.activity.vo.review.WorkEnrolledListItemVO;
import com.example.activity.vo.review.WorkReviewActionVO;
import com.example.activity.vo.review.WorkReviewListItemVO;
import org.springframework.stereotype.Component;

@Component
public class WorkReviewConverter {

    public WorkReviewListItemVO toListItemVO(Work work, Activity activity) {
        WorkReviewListItemVO vo = new WorkReviewListItemVO();
        vo.setId(work.getId());
        vo.setActivityId(work.getActivityId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setTitle(work.getTitle());
        vo.setCategory(work.getCategory());
        vo.setTeacherId(work.getTeacherId());
        vo.setProvinceName(work.getProvinceName());
        vo.setCityName(work.getCityName());
        vo.setDistrictName(work.getDistrictName());
        vo.setSchoolName(work.getSchoolName());
        vo.setCurrentStep(work.getCurrentStep());
        vo.setCurrentStatus(work.getCurrentStatus());
        vo.setCreatedAt(work.getCreatedAt());
        vo.setUpdatedAt(work.getUpdatedAt());
        return vo;
    }

    public WorkEnrolledListItemVO toEnrolledListItemVO(Work work, Activity activity) {
        WorkEnrolledListItemVO vo = new WorkEnrolledListItemVO();
        vo.setId(work.getId());
        vo.setActivityId(work.getActivityId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setTitle(work.getTitle());
        vo.setCategory(work.getCategory());
        vo.setTeacherId(work.getTeacherId());
        vo.setProvinceName(work.getProvinceName());
        vo.setCityName(work.getCityName());
        vo.setDistrictName(work.getDistrictName());
        vo.setSchoolName(work.getSchoolName());
        vo.setCurrentStep(work.getCurrentStep());
        vo.setCurrentStatus(work.getCurrentStatus());
        vo.setFinalResult(work.getFinalResult());
        vo.setFinalScore(work.getFinalScore());
        vo.setCreatedAt(work.getCreatedAt());
        vo.setUpdatedAt(work.getUpdatedAt());
        return vo;
    }

    public WorkReviewActionVO toActionVO(Work work) {
        WorkReviewActionVO vo = new WorkReviewActionVO();
        vo.setWorkId(work.getId());
        vo.setCurrentStep(work.getCurrentStep());
        vo.setCurrentStatus(work.getCurrentStatus());
        return vo;
    }
}
