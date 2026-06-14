package com.example.activity.service;

import com.example.activity.common.result.PageResult;
import com.example.activity.dto.query.ScoreWorkPageQuery;
import com.example.activity.dto.request.score.SubmitReviewRequest;
import com.example.activity.vo.score.ScopedScorerStatsVO;
import com.example.activity.vo.score.ScoreWorkListItemVO;
import com.example.activity.vo.score.SubmitReviewResultVO;

public interface ScoreService {

    PageResult<ScoreWorkListItemVO> scoreWorks(ScoreWorkPageQuery query);

    SubmitReviewResultVO submitReview(Long workId, SubmitReviewRequest request);

    ScopedScorerStatsVO listScopedScorers(Long workId);
}
