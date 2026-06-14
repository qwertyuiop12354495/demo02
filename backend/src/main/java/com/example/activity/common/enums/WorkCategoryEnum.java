package com.example.activity.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkCategoryEnum {

    MUSIC("MUSIC", "音乐"),
    DANCE("DANCE", "舞蹈"),
    ART("ART", "美术"),
    DRAMA("DRAMA", "戏剧"),
    OTHER("OTHER", "其他");

    @EnumValue
    @JsonValue
    private final String value;

    private final String label;

    public static boolean isMusic(String category) {
        if (category == null || category.isBlank()) {
            return false;
        }
        String normalized = category.trim();
        return MUSIC.value.equalsIgnoreCase(normalized)
                || MUSIC.name().equalsIgnoreCase(normalized)
                || MUSIC.label.equals(normalized);
    }
}
