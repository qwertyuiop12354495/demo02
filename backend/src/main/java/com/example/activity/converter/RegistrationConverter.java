package com.example.activity.converter;

import com.example.activity.entity.Activity;
import com.example.activity.entity.ActivityRegistration;
import com.example.activity.entity.SysUser;
import com.example.activity.vo.registration.RegistrationCancelVO;
import com.example.activity.vo.registration.RegistrationListItemVO;
import com.example.activity.vo.registration.RegistrationVO;
import org.springframework.stereotype.Component;

@Component
public class RegistrationConverter {

    public RegistrationVO toVO(ActivityRegistration registration, Activity activity) {
        RegistrationVO vo = new RegistrationVO();
        vo.setId(registration.getId());
        vo.setActivityId(registration.getActivityId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setUserId(registration.getUserId());
        vo.setStatus(registration.getStatus());
        vo.setApplyTime(registration.getApplyTime());
        vo.setAuditTime(registration.getAuditTime());
        vo.setAuditRemark(registration.getAuditRemark());
        vo.setRemark(registration.getApplyRemark());
        vo.setAuditedBy(registration.getAuditedBy());
        return vo;
    }

    public RegistrationCancelVO toCancelVO(ActivityRegistration registration) {
        RegistrationCancelVO vo = new RegistrationCancelVO();
        vo.setId(registration.getId());
        vo.setActivityId(registration.getActivityId());
        vo.setStatus(registration.getStatus());
        vo.setApplyTime(registration.getApplyTime());
        vo.setAuditTime(registration.getAuditTime());
        vo.setAuditRemark(registration.getAuditRemark());
        return vo;
    }

    public RegistrationListItemVO toAdminListItemVO(ActivityRegistration registration, SysUser user) {
        RegistrationListItemVO vo = new RegistrationListItemVO();
        vo.setId(registration.getId());
        vo.setUserId(registration.getUserId());
        vo.setUsername(user != null ? user.getUsername() : null);
        vo.setNickname(user != null ? user.getNickname() : null);
        vo.setStatus(registration.getStatus());
        vo.setApplyTime(registration.getApplyTime());
        vo.setAuditTime(registration.getAuditTime());
        vo.setAuditRemark(registration.getAuditRemark());
        vo.setRemark(registration.getApplyRemark());
        vo.setAuditedBy(registration.getAuditedBy());
        return vo;
    }

    public RegistrationListItemVO toListItemVO(ActivityRegistration registration, Activity activity) {
        RegistrationListItemVO vo = new RegistrationListItemVO();
        vo.setId(registration.getId());
        vo.setActivityId(registration.getActivityId());
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setActivityLocation(activity != null ? activity.getLocation() : null);
        vo.setEventStartTime(activity != null ? activity.getStartTime() : null);
        vo.setUserId(registration.getUserId());
        vo.setStatus(registration.getStatus());
        vo.setApplyTime(registration.getApplyTime());
        vo.setAuditTime(registration.getAuditTime());
        vo.setAuditRemark(registration.getAuditRemark());
        vo.setRemark(registration.getApplyRemark());
        vo.setAuditedBy(registration.getAuditedBy());
        return vo;
    }
}
