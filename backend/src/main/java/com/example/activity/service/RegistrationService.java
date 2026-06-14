package com.example.activity.service;

import com.example.activity.common.result.PageResult;
import com.example.activity.dto.query.AdminRegistrationPageQuery;
import com.example.activity.dto.query.RegistrationMinePageQuery;
import com.example.activity.dto.request.registration.RegistrationAuditRequest;
import com.example.activity.dto.request.registration.RegistrationCreateRequest;
import com.example.activity.vo.registration.RegistrationAdminListVO;
import com.example.activity.vo.registration.RegistrationCancelVO;
import com.example.activity.vo.registration.RegistrationListItemVO;
import com.example.activity.vo.registration.RegistrationVO;

public interface RegistrationService {

    RegistrationVO register(RegistrationCreateRequest request, Long userId);

    RegistrationCancelVO cancel(Long registrationId, Long userId);

    PageResult<RegistrationListItemVO> pageMine(RegistrationMinePageQuery query, Long userId);

    RegistrationAdminListVO pageByActivity(Long activityId, AdminRegistrationPageQuery query);

    RegistrationVO audit(Long registrationId, RegistrationAuditRequest request, Long adminId);
}
