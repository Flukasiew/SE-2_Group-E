package com.pw.common.model;

public class Field {
    public Position position;
    public Cell cell;
    public FieldColor fieldColor;

    public enum FieldColor {
        RED,
        BLUE,
        GRAY
    }

    public Field() {

    }
}