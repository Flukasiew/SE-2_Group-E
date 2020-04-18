package com.pw.common.model;

import lombok.Data;

@Data
public class Board {
    public Cell[][] cellsGrid;
    public int goalAreaHeight;
    public int taskAreaHeight;
    public int boardHeight;
    public int boardWidth;

    public Board(int boardWidth, int goalAreaHeight, int taskAreaHeight){
        this.boardWidth = boardWidth;
        this.boardHeight = goalAreaHeight+2*taskAreaHeight;
        this.goalAreaHeight = goalAreaHeight;
        this.taskAreaHeight = taskAreaHeight;
        this.initializeCellsGrid();
    }

    private Cell getCell(Position position){
        return this.cellsGrid[position.x][position.y];
    }

    private void updateCell(Cell cell, Position position) {
        this.cellsGrid[position.x][position.y] = cell;
    }


    private void initializeCellsGrid() {
        cellsGrid = new Cell[boardWidth][boardHeight];
        for(int i=0;i<boardWidth;i++)
        {
            for(int j=0; j<boardHeight;j++){
                cellsGrid[i][j] =  new Cell();
            }
        }
    }

    public void updateField(Field field) {
        updateCell(field.cell, field.position);
    }

    public Field getField(Position position){
        return new Field(position, getCell(position));
    }

}
