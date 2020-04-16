package com.pw.common.model;

import lombok.Data;

@Data
public class Cell {
    public CellState cellState;
    public int distance;
    public String playerGuids;

    public enum CellState {
        EMPTY,
        GOAL,
        PIECE,
        VALID,
        UNKNOWN
    }

    public Cell() {

    }
}
