package com.example.activity.mapper.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionSummaryRow {

    private Long workId;

    private String workTitle;

    private String schoolName;

    private String districtName;

    private String cityName;

    private String provinceName;

    private BigDecimal averageScore;

    private LocalDateTime publishedAt;
}
