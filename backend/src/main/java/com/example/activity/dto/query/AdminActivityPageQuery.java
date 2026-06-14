package com.example.activity.dto.query;

import com.example.activity.common.enums.ActivityStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminActivityPageQuery extends ActivityPageQuery {

    private ActivityStatus status;
}
