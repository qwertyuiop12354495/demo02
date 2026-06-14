package com.example.activity.vo.activity;

import com.example.activity.common.enums.ActivityStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminActivityListItemVO extends UserActivityListItemVO {

    private ActivityStatus status;
}
