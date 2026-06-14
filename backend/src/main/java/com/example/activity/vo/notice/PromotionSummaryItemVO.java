package com.example.activity.vo.notice;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionSummaryItemVO {

    private Long workId;

    private String workTitle;

    private String schoolName;

    private String districtName;

    private String cityName;

    private String provinceName;

    private BigDecimal averageScore;

    private LocalDateTime publishedAt;
}
