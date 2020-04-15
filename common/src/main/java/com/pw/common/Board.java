package com.pw.common;

import lombok.Data;

@Data
public class Board {
    public Cell[][] cellsGrid;
    public int goalAreaHeight;
    public int taskAreaHeight;
    public int boardHeight;
    public int boardWidth;

    // ??? not sure about this one
    public Board() {

    }

    public void updateField(Field field) {

    }

    private void updateCell(Cell cell, Position position) {

    }

    private void initializeCellsGrid() {

    }
}
