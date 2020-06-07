package com.pw.gamemaster.model;

import com.pw.common.dto.PlayerDTO;
import com.pw.common.model.Cell;
import com.pw.common.model.Field;
import com.pw.common.model.Position;
import com.pw.common.model.TeamColor;
import org.junit.jupiter.api.Test;
import org.junit.Before;
import org.mockito.Mock;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.awt.*;
import java.io.Writer;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


class GameMasterBoardTest {


    @Test
    void playerMoveWithinBounds() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        Position ret;
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerGuid(UUID.randomUUID());
        player_blue.setPlayerTeamColor(TeamColor.Blue);

        PlayerDTO player_red = new PlayerDTO();
        player_red.setPlayerGuid(UUID.randomUUID());
        player_red.setPlayerTeamColor(TeamColor.Red);

        //Within common space
        player_blue.setPosition(new Position(5,5));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.DOWN);
        assertEquals(5,ret.getX());
        assertEquals(6,ret.getY());

        player_blue.setPosition(new Position(5,5));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.UP);
        assertEquals(5,ret.getX());
        assertEquals(4,ret.getY());

        player_blue.setPosition(new Position(5,5));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.LEFT);
        assertEquals(4,ret.getX());
        assertEquals(5,ret.getY());

        player_blue.setPosition(new Position(5,5));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.RIGHT);
        assertEquals(6,ret.getX());
        assertEquals(5,ret.getY());

        player_red.setPosition(new Position(8,8));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.DOWN);
        assertEquals(8,ret.getX());
        assertEquals(9,ret.getY());

        player_red.setPosition(new Position(8,8));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.UP);
        assertEquals(8,ret.getX());
        assertEquals(7,ret.getY());

        player_red.setPosition(new Position(8,8));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.LEFT);
        assertEquals(7,ret.getX());
        assertEquals(8,ret.getY());

        player_red.setPosition(new Position(8,8));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.RIGHT);
        assertEquals(9,ret.getX());
        assertEquals(8,ret.getY());

        //Blue within its Goal Area
        player_blue.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.DOWN);;
        assertEquals(2,ret.getX());
        assertEquals(3,ret.getY());

        player_blue.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.UP);
        assertEquals(2,ret.getX());
        assertEquals(1,ret.getY());

        player_blue.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.LEFT);
        assertEquals(1,ret.getX());
        assertEquals(2,ret.getY());

        player_blue.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.RIGHT);
        assertEquals(3,ret.getX());
        assertEquals(2,ret.getY());

        //Red within its goal area
        player_red.setPosition(new Position(12,22));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.DOWN);;
        assertEquals(12,ret.getX());
        assertEquals(23,ret.getY());

        player_red.setPosition(new Position(12,22));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.UP);
        assertEquals(12,ret.getX());
        assertEquals(21,ret.getY());

        player_red.setPosition(new Position(12,22));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.LEFT);
        assertEquals(11,ret.getX());
        assertEquals(22,ret.getY());

        player_red.setPosition(new Position(12,22));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.RIGHT);
        assertEquals(13,ret.getX());
        assertEquals(22,ret.getY());

    }

    @Test
    void playerMoveOutOfBounds(){
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        Position ret;
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerGuid(UUID.randomUUID());
        player_blue.setPlayerTeamColor(TeamColor.Blue);

        PlayerDTO player_red = new PlayerDTO();
        player_red.setPlayerGuid(UUID.randomUUID());
        player_red.setPlayerTeamColor(TeamColor.Red);


        player_blue.setPosition(new Position(11,19));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.DOWN);;
        assertNull(ret);

        player_blue.setPosition(new Position(1,0));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.UP);
        assertNull(ret);

        player_blue.setPosition(new Position(0,5));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.LEFT);
        assertNull(ret);

        player_blue.setPosition(new Position(15,6));
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.RIGHT);
        assertNull(ret);


        player_red.setPosition(new Position(15,23));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.DOWN);;
        assertNull(ret);

        player_red.setPosition(new Position(4,4));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.UP);
        assertNull(ret);

        player_red.setPosition(new Position(0,5));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.LEFT);
        assertNull(ret);

        player_red.setPosition(new Position(15,7));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.RIGHT);
        assertNull(ret);
    }

    @Test
    void playerMoveOnPlayer() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        Position ret;
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerGuid(UUID.randomUUID());
        player_blue.setPosition(new Position(7,8));
        player_blue.setPlayerTeamColor(TeamColor.Blue);

        PlayerDTO player_red = new PlayerDTO();
        player_red.setPlayerGuid(UUID.randomUUID());
        player_red.setPosition(new Position(7,8));
        player_red.setPlayerTeamColor(TeamColor.Red);


        gameMasterBoard.cellsGrid[8][8].setPlayerGuids(UUID.randomUUID());
        ret = gameMasterBoard.playerMove(player_blue, Position.Direction.RIGHT);
        assertEquals(8,ret.getX());
        assertEquals(8,ret.getY());

        gameMasterBoard.cellsGrid[8][8].setPlayerGuids(UUID.randomUUID());
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.RIGHT);
        assertEquals(8,ret.getX());
        assertEquals(8,ret.getY());
    }


    @Test
    void testTakePiece() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        Cell.CellState ret;
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerGuid(UUID.randomUUID());
        player_blue.setPlayerTeamColor(TeamColor.Blue);

        PlayerDTO player_red = new PlayerDTO();
        player_red.setPlayerGuid(UUID.randomUUID());
        player_red.setPlayerTeamColor(TeamColor.Red);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.PIECE;
        ret = gameMasterBoard.takePiece(new Position(5,5));
        assertEquals(ret,Cell.CellState.PIECE );

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.EMPTY;
        ret = gameMasterBoard.takePiece(new Position(5,5));
        assertEquals(ret,Cell.CellState.EMPTY);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.UNKNOWN;
        ret = gameMasterBoard.takePiece(new Position(5,5));
        assertEquals(ret,Cell.CellState.UNKNOWN);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.GOAL;
        ret = gameMasterBoard.takePiece(new Position(5,5));
        assertEquals(ret,Cell.CellState.GOAL);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.VALID;
        ret = gameMasterBoard.takePiece(new Position(5,5));
        assertEquals(ret,Cell.CellState.VALID);


    }

    @Test
    void generatePiece() {
        Random generator = new Random(11);
        GameMasterBoard gameMasterBoard = new GameMasterBoard(4,1, 2);

        for(int i=0;i<10;i++)
        {
            gameMasterBoard.generatePiece();
        }

        Set<Position> gen_pieces = gameMasterBoard.getPiecesPosition();
        assertEquals(8,gen_pieces.size());

        for (Position piece : gen_pieces){
            int no_copies = 0;
            for (Position inner_piece : gen_pieces){
                if(piece.getX() == inner_piece.getX() && piece.getY() == inner_piece.getY() ) no_copies+=1;
            }
            assertEquals(1,no_copies);
        }
    }

    @Test // Incomplete
    void placePiece() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(4,3, 3);
        PlayerDTO player_blue = new PlayerDTO();

        //Correct
        gameMasterBoard.setGoal(new Position( 0,0));
        player_blue.setPosition(new Position(0,0));
        assertEquals( PlacementResult.CORRECT, gameMasterBoard.placePiece(player_blue));
        gameMasterBoard.setGoal(new Position( 3,2));
        player_blue.setPosition(new Position(3,2));
        PlacementResult out =  gameMasterBoard.placePiece(player_blue);
        assertEquals( PlacementResult.CORRECT, out);

    }

    @Test
    void setGoal() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerGuid(UUID.randomUUID());
        player_blue.setPlayerTeamColor(TeamColor.Blue);

        PlayerDTO player_red = new PlayerDTO();
        player_red.setPlayerGuid(UUID.randomUUID());
        player_red.setPlayerTeamColor(TeamColor.Red);


        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.PIECE;
        gameMasterBoard.setGoal(new Position(5,5));
        assertEquals(gameMasterBoard.cellsGrid[5][5].cellState,Cell.CellState.GOAL);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.EMPTY;
        gameMasterBoard.setGoal(new Position(5,5));
        assertEquals(gameMasterBoard.cellsGrid[5][5].cellState,Cell.CellState.GOAL);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.UNKNOWN;
        gameMasterBoard.setGoal(new Position(5,5));
        assertEquals(gameMasterBoard.cellsGrid[5][5].cellState,Cell.CellState.GOAL);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.GOAL;
        gameMasterBoard.setGoal(new Position(5,5));
        assertEquals(gameMasterBoard.cellsGrid[5][5].cellState,Cell.CellState.GOAL);

        gameMasterBoard.cellsGrid[5][5].cellState = Cell.CellState.VALID;
        gameMasterBoard.setGoal(new Position(5,5));
        assertEquals(gameMasterBoard.cellsGrid[5][5].cellState,Cell.CellState.GOAL);


        gameMasterBoard.setGoal(new Position(1,1));
        assertEquals(gameMasterBoard.cellsGrid[5][5].cellState,Cell.CellState.GOAL);
    }

    @Test
    void placePlayer() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        Position ret;
        Position pos;
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerGuid(UUID.randomUUID());
        player_blue.setPlayerTeamColor(TeamColor.Blue);

        PlayerDTO player_red = new PlayerDTO();
        player_red.setPlayerGuid(UUID.randomUUID());
        player_red.setPlayerTeamColor(TeamColor.Red);


        //Occupied spot
        pos = new Position(7,7);
        player_blue.setPosition(pos);
        gameMasterBoard.cellsGrid[pos.getX()][pos.getY()].setPlayerGuids(UUID.randomUUID());
        ret = gameMasterBoard.placePlayer(player_blue);
        assertEquals(ret,null);

        //free spot
        pos = new Position(10,10);
        player_blue.setPosition(pos);
        ret = gameMasterBoard.placePlayer(player_blue);
        assertEquals(ret.getX(),pos.getX());
        assertEquals(ret.getY(),pos.getY());

        //Invalid position
        pos = new Position(20,7);
        player_blue.setPosition(pos);
        ret = gameMasterBoard.placePlayer(player_blue);
        assertEquals(ret,null);


    }

    @Test
    void checkWinCondition() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        Position pos;
        PlayerDTO dto = new PlayerDTO();

        //becouse Blue has zero goals hence all are filled
        boolean ret = gameMasterBoard.checkWinCondition(TeamColor.Blue);
        assertEquals(true,ret);


        gameMasterBoard.setGoal(new Position(3,1));
        gameMasterBoard.setGoal(new Position(1,2));
        gameMasterBoard.setGoal(new Position(2,3));
        gameMasterBoard.setGoal(new Position(1,0));

        dto.setPosition(new Position(3,1));
        gameMasterBoard.placePiece(dto);
        dto.setPosition(new Position(1,2));
        gameMasterBoard.placePiece(dto);
        dto.setPosition(new Position(2,3));
        gameMasterBoard.placePiece(dto);
        //because not all goal filled
        ret = gameMasterBoard.checkWinCondition(TeamColor.Blue);
        assertEquals(false,ret);

        dto.setPosition(new Position(1,0));
        gameMasterBoard.placePiece(dto);
        //all goals are filled
        ret = gameMasterBoard.checkWinCondition(TeamColor.Blue);
        assertEquals(true,ret);

        //becouse red has zero goals hence all are filled
        ret = gameMasterBoard.checkWinCondition(TeamColor.Red);
        assertEquals(true,ret);

        gameMasterBoard.setGoal(new Position(13,22));
        gameMasterBoard.setGoal(new Position(10,21));
        gameMasterBoard.setGoal(new Position(1,21));
        //because not all goals are filled
        ret = gameMasterBoard.checkWinCondition(TeamColor.Red);
        assertEquals(false,ret);

        dto.setPosition(new Position(13,22));
        gameMasterBoard.placePiece(dto);
        dto.setPosition(new Position(10,21));
        gameMasterBoard.placePiece(dto);
        //because not all goals are filled
        ret = gameMasterBoard.checkWinCondition(TeamColor.Red);
        assertEquals(false,ret);

        dto.setPosition(new Position(1,21));
        gameMasterBoard.placePiece(dto);
        //because all goals are filled
        ret = gameMasterBoard.checkWinCondition(TeamColor.Red);
        assertEquals(true,ret);
    }

    @Test
    void discover() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        List<Field> ret = gameMasterBoard.discover(new Position(6,5));
        for(int i=0;i<ret.size();i++)
        {
            assertEquals(-1,ret.get(i).getCell().distance);
        }

        Position new_piece_position;
        new_piece_position = new Position(5,5);
        gameMasterBoard.piecesPosition.add(new_piece_position);
        gameMasterBoard.cellsGrid[new_piece_position.getX()][new_piece_position.getY()].setCellState(Cell.CellState.PIECE);

        new_piece_position = new Position(5,6);
        gameMasterBoard.piecesPosition.add(new_piece_position);
        gameMasterBoard.cellsGrid[new_piece_position.getX()][new_piece_position.getY()].setCellState(Cell.CellState.PIECE);

        new_piece_position = new Position(9,6);
        gameMasterBoard.piecesPosition.add(new_piece_position);
        gameMasterBoard.cellsGrid[new_piece_position.getX()][new_piece_position.getY()].setCellState(Cell.CellState.PIECE);

        ret = gameMasterBoard.discover(new Position(6,5));
        int[] outs = {1,0,0,2,1,1,3,2,2};
        for(int i=0;i<ret.size();i++)
        {
            assertEquals(outs[i],ret.get(i).getCell().distance);
        }



    }

    @Test
    void manhattanDistanceTwoPoints() {
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,4,16);
        Point a;
        Point b;

        a = new Point(1,2);
        b = new Point (3,2);
        assertEquals(2,gameMasterBoard.manhattanDistanceTwoPoints(a,b));

        a = new Point(1,2);
        b = new Point (1,2);
        assertEquals(0,gameMasterBoard.manhattanDistanceTwoPoints(a,b));

        a = new Point(1,2);
        b = new Point (7,3);
        assertEquals(7,gameMasterBoard.manhattanDistanceTwoPoints(a,b));

        a = new Point(8,3);
        b = new Point (5,11);
        assertEquals(11,gameMasterBoard.manhattanDistanceTwoPoints(a,b));

        a = new Point(1,2);
        b = new Point (1,8);
        assertEquals(6,gameMasterBoard.manhattanDistanceTwoPoints(a,b));

        a = new Point(15,1);
        b = new Point (7,2);
        assertEquals(9,gameMasterBoard.manhattanDistanceTwoPoints(a,b));
    }
}