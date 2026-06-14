package com.example.activity.vo.registration;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationAdminListVO {

    private ActivitySummaryVO activity;

    private List<RegistrationListItemVO> list;

    private long total;

    private long page;

    private long pageSize;

    @Data
    public static class ActivitySummaryVO {

        private Long id;

        private String title;

        private Integer maxParticipants;

        private Integer approvedCount;

        private Integer pendingCount;
    }
}
