package com.example.activity.vo.score;

import com.example.activity.common.enums.ReviewLevelEnum;
import lombok.Data;

@Data
public class ScopedScorerStatsVO {

    private ReviewLevelEnum reviewLevel;

    private int requiredCount;

    private int completedCount;
}
