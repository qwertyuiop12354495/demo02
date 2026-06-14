package com.example.activity.converter;

import com.example.activity.entity.Activity;
import com.example.activity.entity.Work;
import com.example.activity.vo.score.ScoreWorkListItemVO;
import com.example.activity.vo.score.SubmitReviewResultVO;
import org.springframework.stereotype.Component;

@Component
public class ScoreConverter {

    public ScoreWorkListItemVO toListItemVO(Work work, Activity activity) {
        ScoreWorkListItemVO vo = new ScoreWorkListItemVO();
        vo.setId(work.getId());
        vo.setActivityId(work.getActivityId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setTitle(work.getTitle());
        vo.setCategory(work.getCategory());
        vo.setTeacherId(work.getTeacherId());
        vo.setCurrentStep(work.getCurrentStep());
        vo.setCurrentStatus(work.getCurrentStatus());
        vo.setFinalResult(work.getFinalResult());
        vo.setUpdatedAt(work.getUpdatedAt());
        return vo;
    }

    public SubmitReviewResultVO toSubmitResult(Work work, int requiredCount, int completedCount,
                                               boolean allCompleted, String message) {
        SubmitReviewResultVO vo = new SubmitReviewResultVO();
        vo.setWorkId(work.getId());
        vo.setCurrentStep(work.getCurrentStep());
        vo.setCurrentStatus(work.getCurrentStatus());
        vo.setFinalResult(work.getFinalResult());
        vo.setFinalScore(work.getFinalScore());
        vo.setRequiredCount(requiredCount);
        vo.setCompletedCount(completedCount);
        vo.setAllCompleted(allCompleted);
        vo.setMessage(message);
        return vo;
    }
}
