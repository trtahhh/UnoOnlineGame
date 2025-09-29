package com.uno.model;

/**
 * Enum cho loại lá bài Uno
 */
public enum CardType {
    NUMBER("Số"),
    SKIP("Bỏ lượt"),
    REVERSE("Đảo chiều"),
    DRAW_TWO("Rút 2"),
    WILD("Đổi màu"),
    WILD_DRAW_FOUR("Rút 4");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}