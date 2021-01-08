package com.app.memoeslink.notemanagement;

public enum DrawablePosition {
    LEFT(0),
    TOP(1),
    RIGHT(2),
    BOTTOM(3);

    private int value;

    private DrawablePosition(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
