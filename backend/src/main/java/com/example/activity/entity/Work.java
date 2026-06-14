package com.example.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.activity.common.enums.FinalResultEnum;
import com.example.activity.common.enums.WorkStatusEnum;
import com.example.activity.common.enums.WorkStepEnum;
import com.example.activity.common.scope.ScopedData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work")
public class Work implements ScopedData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long teacherId;

    private String title;

    private String category;

    private String equipment;

    private Integer duration;

    private String provinceName;

    private String cityName;

    private String districtName;

    private String schoolName;

    private WorkStepEnum currentStep;

    private WorkStatusEnum currentStatus;

    private BigDecimal finalScore;

    private FinalResultEnum finalResult;

    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Long activeTeacherId;

    @TableField(exist = false)
    private Long activeActivityId;
}
