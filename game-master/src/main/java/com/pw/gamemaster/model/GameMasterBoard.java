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
    public Set<Position> piecesPosition;
    public HashSet<UUID> piecesTested;

    public GameMasterBoard(int boardWidth, int goalAreaHeight, int taskAreaHeight) {
        super(boardWidth, goalAreaHeight, taskAreaHeight);
        this.piecesTested = new HashSet<>();
        this.piecesPosition = new HashSet<>();
    }

    public Position playerMove(PlayerDTO playerDTO, Position.Direction direction) {;
        Position old_pos = new Position(playerDTO.playerPosition.getX(),playerDTO.playerPosition.getY());
        playerDTO.playerPosition.changePosition(direction);

        if(playerDTO.playerPosition.getX()<0 || playerDTO.playerPosition.getX()>=this.boardWidth ||
                playerDTO.playerPosition.getY()<0 || playerDTO.playerPosition.getY()>=this.boardHeight)
        {
            playerDTO.playerPosition = old_pos;
            return null;
        }

//        if(this.getField(playerDTO.getPosition()).cell.playerGuids != null){
//            return null;
//        }
        else
        {
            if (playerDTO.playerTeamColor == TeamColor.Blue)
            {
                if (playerDTO.playerPosition.getY() < this.boardHeight - this.goalAreaHeight)
                {
                    this.cellsGrid[playerDTO.getPosition().getX()][playerDTO.getPosition().getY()].setPlayerGuids(playerDTO.playerGuid);
                    this.cellsGrid[old_pos.getX()][old_pos.getY()].setPlayerGuids(null);
                    return playerDTO.playerPosition;
                }
                else {
                    playerDTO.playerPosition = old_pos;
                    return null;
                }

            }
            else
            {
                if (playerDTO.playerPosition.getY()>=this.goalAreaHeight) {
                    {
                        this.cellsGrid[playerDTO.getPosition().getX()][playerDTO.getPosition().getY()].setPlayerGuids(playerDTO.playerGuid);
                        this.cellsGrid[old_pos.getX()][old_pos.getY()].setPlayerGuids(null);
                        return playerDTO.playerPosition;
                    }
                }

                else {
                    playerDTO.playerPosition = old_pos;
                    return null;
                }
            }
        }

    }

    public Cell.CellState takePiece(Position position) {
        Cell.CellState init_state =  this.getField(position).cell.getCellState();

        if(this.getField(position).cell.getCellState() == Cell.CellState.Piece){
            this.cellsGrid[position.getX()][position.getY()].setCellState(Cell.CellState.Empty);
            for (Position p : this.piecesPosition){
                if(p.x == position.getX() && p.y == position.getY()) {
                    this.piecesPosition.remove(p);
                    break;
                }
            }
        }

        return init_state;
    }

    public Position generatePiece() {
        Position new_piece_position;

        Position[] pos = new Position[this.boardWidth*this.taskAreaHeight];
        for (int i = 0; i < this.boardWidth; i++) {
            for (int j = 0; j < this.taskAreaHeight; j++) {
                Position temp = new Position();
                temp.x = i;
                temp.y = j + this.goalAreaHeight;
                pos[i * (this.boardHeight - 2 * this.goalAreaHeight) + j] = temp;

            }
        }

        Collections.shuffle(Arrays.asList(pos));
        for (Position p : pos) {
            int exists = 0;
            new_piece_position = p;
            for (Position piece : piecesPosition) {
                if(piece.x == new_piece_position.getX() && piece.y == new_piece_position.getY()){
                    exists =1;
                    break;
                }
            }
            if(exists == 0)
            {

                piecesPosition.add(new_piece_position);
                this.cellsGrid[new_piece_position.getX()][new_piece_position.getY()].setCellState(Cell.CellState.Piece);
                return new_piece_position;
            }
        }

        return null;
    }

    public void setGoal(Position position) {
        this.cellsGrid[position.getX()][position.getY()].cellState = Cell.CellState.Goal;
    }

    public PlacementResult placePiece(PlayerDTO playerDTO) {

        Field current_field = this.getField(playerDTO.playerPosition);
        if(current_field == null) return PlacementResult.POINTLESS;
        if (current_field.cell.getCellState() == Cell.CellState.Goal){
            current_field.cell.setCellState(Cell.CellState.Empty);
            this.updateField(current_field);
            return PlacementResult.CORRECT;
        }
        else {
            return PlacementResult.POINTLESS;
        }
    }

    public Position placePlayer(PlayerDTO playerDTO) {
        if (this.getField(playerDTO.getPosition()) == null)return null;
        if(this.getField(playerDTO.getPosition()).cell.getplayerGuids() !=null){
            return null;
        }
        this.cellsGrid[playerDTO.playerPosition.getX()][playerDTO.playerPosition.getY()].setPlayerGuids((playerDTO.playerGuid));

        return playerDTO.playerPosition;
    }

    public boolean checkWinCondition(TeamColor teamColor) {
        if(teamColor == TeamColor.Blue) {
            for(int i=0;i<this.boardWidth;i++)
            {
                for(int j=0;j<this.goalAreaHeight;j++)
                {
                    Position temp = new Position(i,j);
                    if(this.getField(temp).cell.getCellState() == Cell.CellState.Goal) {
                        int filled = 0;
                        for (Position piece : piecesPosition) {
                            if (i == piece.getX() && j == piece.getY()) {
                                filled = 1;
                                break;
                            }
                        }
                        if(filled == 0) return false;

                    }
                }
            }
            return true;
        }
        else
        {
            for(int i=0;i<this.boardWidth;i++)
            {
                for(int j=this.boardHeight-this.goalAreaHeight;j<this.boardHeight;j++)
                {
                    Position temp = new Position(i,j);
                    if(this.getField(temp).cell.getCellState() == Cell.CellState.Goal) {
                        int filled = 0;
                        for (Position piece : piecesPosition) {
                            if (i == piece.getY() && j == piece.y) {
                                filled = 1;
                                break;
                            }
                        }
                        if(filled == 0) return false;

                    }
                }
            }
            return true;
        }
    }

    public List<Field> discover(Position position) {
        List<Field> discovered_fields = new ArrayList<>();
        for (int i=-1;i<=1;i++){
            for(int j=-1;j<=1;j++)
            {
                Position new_pos = new Position();
                new_pos.setX(position.getX()+i);
                new_pos.setY(position.getY()+j);
                if(new_pos.getX()<this.boardWidth && new_pos.getX()>=0 && new_pos.y>=0 && new_pos.getY()<this.boardHeight){
                    int min_dist = this.boardWidth+this.boardHeight;
                    Point curr = new Point(new_pos.x,new_pos.y);
                    for(int a = 0; a<this.boardWidth;a++)
                    {
                        for(int b=this.goalAreaHeight; b<this.boardHeight-this.goalAreaHeight;b++)
                        {
                            //System.out.println(this.getField(new Position(a,b)).getCell().cellState);
                            if(this.getField(new Position(a,b)).getCell().cellState == Cell.CellState.Piece)
                            {
                                Point temp = new Point(a,b);
                                //System.out.println(manhattanDistanceTwoPoints(curr,temp));
                                if(manhattanDistanceTwoPoints(curr,temp)<min_dist)
                                    min_dist=manhattanDistanceTwoPoints(curr,temp);
                            }
                        }
                    }
                    if(min_dist == this.boardWidth+this.boardHeight) min_dist =-1;
                    this.getField(new_pos).getCell().distance = min_dist;
                    discovered_fields.add(this.getField(new_pos));
                }
            }
        }
        return discovered_fields;
    }

    public int manhattanDistanceTwoPoints(Point pointA, Point pointB) {
        return Math.abs(pointA.x-pointB.x) + Math.abs(pointA.y-pointB.y);
    }

    public Set<Position> getPiecesPosition() {
        return this.piecesPosition;
    }
}