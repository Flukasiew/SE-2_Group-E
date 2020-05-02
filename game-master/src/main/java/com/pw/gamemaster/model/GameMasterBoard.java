package com.pw.gamemaster.model;

import com.pw.common.dto.PlayerDTO;
import com.pw.common.model.Board;
import com.pw.common.model.Cell;
import com.pw.common.model.Field;
import com.pw.common.model.Position;
import com.pw.common.model.TeamColor;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Random;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;

public class GameMasterBoard extends Board {
    private Set<Position> piecesPosition;
    public HashSet<UUID> piecesTested;

    public GameMasterBoard(int boardWidth, int goalAreaHeight, int taskAreaHeight) {
        super(boardWidth, goalAreaHeight, taskAreaHeight);
        this.piecesTested = new HashSet<UUID>();
        this.piecesPosition = new HashSet<Position>();
    }

    public Position playerMove(PlayerDTO playerDTO, Position.Direction direction) {
        playerDTO.playerPosition.changePosition(direction);
        Position error_position = new Position();
        error_position.x =-1;
        error_position.y =-1;

        if(playerDTO.playerPosition.x<0 || playerDTO.playerPosition.x>=this.boardWidth ||
        playerDTO.playerPosition.y<0 || playerDTO.playerPosition.y>=this.boardHeight)
        {
            return error_position;
        }

        if (playerDTO.playerTeamColor == TeamColor.BLUE)
        {
            if (playerDTO.playerPosition.y>=this.taskAreaHeight) return playerDTO.playerPosition;
            else return error_position;
        }
        else
        {
            if (playerDTO.playerPosition.y<=this.boardHeight - this.taskAreaHeight) return playerDTO.playerPosition;
            else return error_position;
        }
    }

    public Cell.CellState takePiece(Position position) {
        Cell.CellState init_state =  this.getField(position).cell.cellState;

        if(this.getField(position).cell.cellState== Cell.CellState.PIECE ||
                this.getField(position).cell.cellState== Cell.CellState.VALID){
            this.cellsGrid[position.x][position.y].cellState = Cell.CellState.EMPTY;
            for (Position p : this.piecesPosition){
                if(p.x == position.x && p.y == position.y) {
                    this.piecesPosition.remove(p);
                    break;
                }
            }
        }

        return init_state;
    }

    public Position generatePiece() {
        Position new_piece_position = new Position();

        Position[] pos = new Position[this.boardWidth*(this.boardHeight-2*this.taskAreaHeight)];
        for (int i = 0; i < this.boardWidth; i++) {
            for(int j = 0; j < this.boardHeight-2*this.taskAreaHeight; j++)
            {
                Position temp = new Position();
                temp.x = i;
                temp.y = j+this.taskAreaHeight;
                pos[i*(this.boardHeight-2*this.taskAreaHeight)+j] = temp;

            }
        }

        Collections.shuffle(Arrays.asList(pos));
        for (Position p : pos) {
            new_piece_position = p;
            for (Position piece : piecesPosition) {
                if(piece.x != new_piece_position.x || piece.y != new_piece_position.y){
                    piecesPosition.add(new_piece_position);
                    return new_piece_position;
                }
            }
        }

        new_piece_position.x = -1;
        new_piece_position.y =-1;
        return new_piece_position;
    }

    public void setGoal(Position position) {
        this.getField(position).cell.cellState = Cell.CellState.GOAL;
    }

    public PlacementResult placePiece(PlayerDTO playerDTO) {
        Field current_field = this.getField(playerDTO.playerPosition);
        if (current_field.cell.cellState== Cell.CellState.GOAL){
            current_field.cell.cellState= Cell.CellState.EMPTY;
            this.updateField(current_field);
            return PlacementResult.CORRECT;
        }
        else {
            if (current_field.cell.cellState == Cell.CellState.EMPTY) {

            }
                return PlacementResult.POINTLESS;
        }
    }

    public Position placePlayer(PlayerDTO playerDTO) {
        if(this.getField(playerDTO.playerPosition).fieldColor != Field.FieldColor.GRAY){
            Position error_position = new Position();
            error_position.x =-1;
            error_position.y =-1;
            return error_position;
        }

        switch(playerDTO.playerTeamColor){
            case BLUE:
                this.getField(playerDTO.playerPosition).fieldColor = Field.FieldColor.BLUE;
            case RED:
                this.getField(playerDTO.playerPosition).fieldColor = Field.FieldColor.RED;
        }

        return playerDTO.playerPosition;
    }

    public boolean checkWinCondition(TeamColor teamColor) {
            throw new RuntimeException("Not implemented");

    }

    public List<Field> discover(Position position) {
        List<Field> discovered_fields = new ArrayList<Field>();
        for (int i=-1;i<=1;i++){
            for(int j=-1;j<=1;j++)
            {
                Position new_pos = new Position();
                new_pos.x = position.x-i;
                new_pos.y = position.y-j;
                if(new_pos.x<this.boardWidth && new_pos.x>0 && new_pos.y>0 && new_pos.y<this.boardHeight){
                    discovered_fields.add(this.getField(new_pos));
                }
            }
        }
        return discovered_fields;
    }

    public int manhattanDistanceTwoPoints(Point pointA, Point pointB) {
        return Math.abs(pointA.x-pointB.x) + Math.abs(pointA.y-pointB.y);
    }
}


