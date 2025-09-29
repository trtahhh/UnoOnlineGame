package com.uno.model;

/**
 * Enum cho màu của lá bài Uno
 */
public enum CardColor {
    RED("Đỏ"),
    BLUE("Xanh dương"),
    GREEN("Xanh lá"),
    YELLOW("Vàng"),
    WILD("Đặc biệt");

    private final String displayName;

    CardColor(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}