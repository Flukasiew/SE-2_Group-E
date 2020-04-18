package com.pw.common.model;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;

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
        this.distance=-1;
        this.cellState = CellState.UNKNOWN;
    }

    public CellState getCellState() {
        return this.cellState;
    }

    public void setCellState(CellState cellState) {
        this.cellState = cellState;
    }

    public Field getField(Position position ){
        Field new_field = new Field();
        new_field.cell=this;
        new_field.position=position;
        return new_field;
    }
}
