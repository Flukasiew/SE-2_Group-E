package com.pw.common.model;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;

import java.util.UUID;

@Data
public class Cell {
    public CellState cellState;
    public int distance;
    public UUID playerGuids;

    public enum CellState {
        EMPTY,
        GOAL,
        PIECE,
        VALID,
        UNKNOWN
    }

    public Cell() {
        this.distance=-1;
        this.cellState = CellState.EMPTY;
    }

    public Field getField(Position position ){
        Field new_field = new Field();
        new_field.cell=this;
        new_field.position=position;
        return new_field;
    }

    public CellState getCellState() {
        return this.cellState;
    }

    public void setCellState(CellState cellState) {
        this.cellState = cellState;
    }

    public int getDistance(){ return this.distance;}
    public void setDistance(int distance){ this.distance = distance;}

    public UUID getplayerGuids(){
        return this.playerGuids;
    }

    public void setPlayerGuids(UUID uuid){
        this.playerGuids = uuid;
    }
}
