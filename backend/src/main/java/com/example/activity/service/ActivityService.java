package com.example.activity.service;

import com.example.activity.common.result.PageResult;
import com.example.activity.dto.query.ActivityPageQuery;
import com.example.activity.dto.query.AdminActivityPageQuery;
import com.example.activity.dto.request.activity.ActivityCreateRequest;
import com.example.activity.dto.request.activity.ActivityStatusUpdateRequest;
import com.example.activity.dto.request.activity.ActivityUpdateRequest;
import com.example.activity.vo.activity.ActivityStatusUpdateVO;
import com.example.activity.vo.activity.ActivityVO;
import com.example.activity.vo.activity.AdminActivityListItemVO;
import com.example.activity.vo.activity.UserActivityListItemVO;

public interface ActivityService {

    PageResult<UserActivityListItemVO> pagePublishedActivities(ActivityPageQuery query, Long userId);

    PageResult<AdminActivityListItemVO> pageAdminActivities(AdminActivityPageQuery query);

    ActivityVO getPublishedActivityDetail(Long id, Long userId);

    ActivityVO createActivity(ActivityCreateRequest request, Long adminId);

    ActivityVO updateActivity(Long id, ActivityUpdateRequest request);

    ActivityStatusUpdateVO updateActivityStatus(Long id, ActivityStatusUpdateRequest request);
}
