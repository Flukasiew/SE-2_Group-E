package com.pw.gamemaster.model;

import com.pw.common.dto.PlayerDTO;
import com.pw.common.model.Position;
import com.pw.common.model.TeamColor;
import org.junit.jupiter.api.Test;
import org.junit.Before;
import org.mockito.Mock;
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
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,16,4);
        Position ret;
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerTeamColor(TeamColor.BLUE);

        PlayerDTO player_red = new PlayerDTO();
        player_blue.setPlayerTeamColor(TeamColor.RED);

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
        player_red.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.DOWN);;
        assertEquals(2,ret.getX());
        assertEquals(3,ret.getY());

        player_red.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.UP);
        assertEquals(2,ret.getX());
        assertEquals(1,ret.getY());

        player_red.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.LEFT);
        assertEquals(1,ret.getX());
        assertEquals(2,ret.getY());

        player_red.setPosition(new Position(2,2));
        ret = gameMasterBoard.playerMove(player_red, Position.Direction.RIGHT);
        assertEquals(3,ret.getX());
        assertEquals(2,ret.getY());

    }

    @Test
    void playerMoveOutOfBounds(){
        GameMasterBoard gameMasterBoard = new GameMasterBoard(16,16,4);
        Position ret;
        PlayerDTO player_blue = new PlayerDTO();
        player_blue.setPlayerTeamColor(TeamColor.BLUE);

        PlayerDTO player_red = new PlayerDTO();
        player_red.setPlayerTeamColor(TeamColor.RED);


        player_blue.setPosition(new Position(11,11));
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
    void takePiece() {
    }

    @Test
    void generatePiece() {
    }

    @Test
    void setGoal() {
    }

    @Test
    void placePiece() {
    }

    @Test
    void placePlayer() {
    }

    @Test
    void checkWinCondition() {
    }

    @Test
    void discover() {
    }

    @Test
    void manhattanDistanceTwoPoints() {
    }
}