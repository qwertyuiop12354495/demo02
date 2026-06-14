package com.example.activity.converter;

import com.example.activity.dto.request.activity.ActivityCreateRequest;
import com.example.activity.dto.request.activity.ActivityUpdateRequest;
import com.example.activity.entity.Activity;
import com.example.activity.entity.ActivityRegistration;
import com.example.activity.vo.activity.ActivityStatusUpdateVO;
import com.example.activity.vo.activity.ActivityVO;
import com.example.activity.vo.activity.AdminActivityListItemVO;
import com.example.activity.vo.activity.UserActivityListItemVO;
import com.example.activity.vo.registration.RegistrationBriefVO;
import org.springframework.stereotype.Component;

@Component
public class ActivityConverter {

    public ActivityVO toVO(Activity activity) {
        ActivityVO vo = new ActivityVO();
        fillBaseFields(vo, activity);
        vo.setCreatedBy(activity.getCreatedBy());
        vo.setCreatedAt(activity.getCreatedAt());
        vo.setUpdatedAt(activity.getUpdatedAt());
        return vo;
    }

    public UserActivityListItemVO toUserListItemVO(Activity activity) {
        UserActivityListItemVO vo = new UserActivityListItemVO();
        fillUserListFields(vo, activity);
        return vo;
    }

    public AdminActivityListItemVO toAdminListItemVO(Activity activity) {
        AdminActivityListItemVO vo = new AdminActivityListItemVO();
        fillUserListFields(vo, activity);
        vo.setStatus(activity.getStatus());
        return vo;
    }

    public ActivityStatusUpdateVO toStatusUpdateVO(Activity activity) {
        ActivityStatusUpdateVO vo = new ActivityStatusUpdateVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setStatus(activity.getStatus());
        vo.setUpdatedAt(activity.getUpdatedAt());
        return vo;
    }

    public RegistrationBriefVO toRegistrationBriefVO(ActivityRegistration registration) {
        if (registration == null) {
            return null;
        }
        RegistrationBriefVO vo = new RegistrationBriefVO();
        vo.setId(registration.getId());
        vo.setStatus(registration.getStatus());
        vo.setApplyTime(registration.getApplyTime());
        return vo;
    }

    public Activity fromCreateRequest(ActivityCreateRequest request, Long createdBy) {
        Activity activity = new Activity();
        fillFromRequest(activity, request.getTitle(), request.getDescription(), request.getLocation(),
                request.getEventStartTime(), request.getEventEndTime(),
                request.getRegistrationStartTime(), request.getRegistrationDeadline(),
                request.getMaxParticipants());
        activity.setCreatedBy(createdBy);
        activity.setCurrentCount(0);
        return activity;
    }

    public void updateFromRequest(Activity activity, ActivityUpdateRequest request) {
        fillFromRequest(activity, request.getTitle(), request.getDescription(), request.getLocation(),
                request.getEventStartTime(), request.getEventEndTime(),
                request.getRegistrationStartTime(), request.getRegistrationDeadline(),
                request.getMaxParticipants());
    }

    private void fillBaseFields(ActivityVO vo, Activity activity) {
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setLocation(activity.getLocation());
        vo.setEventStartTime(activity.getStartTime());
        vo.setEventEndTime(activity.getEndTime());
        vo.setRegistrationStartTime(activity.getSignupStartTime());
        vo.setRegistrationDeadline(activity.getSignupEndTime());
        vo.setMaxParticipants(activity.getMaxCount());
        vo.setApprovedCount(activity.getCurrentCount());
        vo.setRemainingSlots(calculateRemainingSlots(activity));
        vo.setStatus(activity.getStatus());
    }

    private void fillUserListFields(UserActivityListItemVO vo, Activity activity) {
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setLocation(activity.getLocation());
        vo.setEventStartTime(activity.getStartTime());
        vo.setEventEndTime(activity.getEndTime());
        vo.setRegistrationStartTime(activity.getSignupStartTime());
        vo.setRegistrationDeadline(activity.getSignupEndTime());
        vo.setMaxParticipants(activity.getMaxCount());
        vo.setApprovedCount(activity.getCurrentCount());
        vo.setRemainingSlots(calculateRemainingSlots(activity));
    }

    private void fillFromRequest(Activity activity, String title, String description, String location,
                                 java.time.LocalDateTime eventStartTime, java.time.LocalDateTime eventEndTime,
                                 java.time.LocalDateTime registrationStartTime,
                                 java.time.LocalDateTime registrationDeadline, Integer maxParticipants) {
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setLocation(location);
        activity.setStartTime(eventStartTime);
        activity.setEndTime(eventEndTime);
        activity.setSignupStartTime(registrationStartTime);
        activity.setSignupEndTime(registrationDeadline);
        activity.setMaxCount(maxParticipants);
    }

    private int calculateRemainingSlots(Activity activity) {
        int approved = activity.getCurrentCount() == null ? 0 : activity.getCurrentCount();
        int max = activity.getMaxCount() == null ? 0 : activity.getMaxCount();
        return Math.max(max - approved, 0);
    }
}
