package com.example.activity.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PublishableActivityStatus {

    PUBLISHED("PUBLISHED"),
    OFFLINE("OFFLINE");

    @JsonValue
    private final String value;

    public ActivityStatus toActivityStatus() {
        return ActivityStatus.valueOf(name());
    }
}
