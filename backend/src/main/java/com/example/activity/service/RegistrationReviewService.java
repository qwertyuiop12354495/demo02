package com.example.activity.service;

import com.example.activity.common.result.PageResult;
import com.example.activity.dto.query.WorkEnrolledPageQuery;
import com.example.activity.dto.query.WorkReviewPageQuery;
import com.example.activity.dto.request.review.WorkRevisionFeedbackRequest;
import com.example.activity.vo.review.WorkEnrolledListItemVO;
import com.example.activity.vo.review.WorkReviewActionVO;
import com.example.activity.vo.review.WorkReviewListItemVO;

public interface RegistrationReviewService {

    PageResult<WorkReviewListItemVO> list(WorkReviewPageQuery query);

    WorkReviewActionVO approve(Long workId);

    WorkReviewActionVO submitRevisionFeedback(Long workId, WorkRevisionFeedbackRequest request);

    PageResult<WorkEnrolledListItemVO> listEnrolled(WorkEnrolledPageQuery query);
}
