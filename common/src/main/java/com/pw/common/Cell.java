package com.pw.common;

import javafx.geometry.Pos;

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

    public CellState getCellState() {
        return cellState;
    }

    public void setCellState(CellState cellState) {

    }

    public Position getField(Position position) {
        return null; // implement
    }
}
