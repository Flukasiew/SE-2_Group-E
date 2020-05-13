package com.pw.player.model;

import com.pw.common.model.*;
import com.pw.player.model.Player;
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

public class PlayerTest {

    @Test
    void PickupPieceTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(6,4));
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.PIECE);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(true, player.piece);
    }


    @Test
    void PickupPieceTestEmpty()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(6,6));
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.EMPTY);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.piece);

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.GOAL);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.piece);

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.VALID);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.piece);

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.UNKNOWN);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.piece);
    }

    @Test
    void PlacePieceTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(2,2));
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.GOAL);
        player.testAction(ActionType.PLACE, null);
        assertEquals(false, player.piece);
        assertEquals(false, player.tested);
    }

    @Test
    void MoveRightTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(4,6));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.RIGHT);
        assertEquals(new Position(5,6).x, player.position.x);
    }

    @Test
    void FalseMoveRightTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(9,6));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.RIGHT);
        assertEquals(new Position(9,6).x, player.position.x);
    }

    @Test
    void MoveLeftTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(4,6));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.LEFT);
        assertEquals(new Position(3,6).x, player.position.x);
    }

    @Test
    void FalseMoveLeftTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(0,6));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.LEFT);
        assertEquals(new Position(0,6).x, player.position.x);
    }

    @Test
    void MoveUpTest()
    {
        Player player = new Player(TeamColor.RED, TeamRole.MEMBER, new Position(3,6));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,5).y, player.position.y);
    }

    @Test
    void FalseMoveUpTest()
    {
        Player player = new Player(TeamColor.RED, TeamRole.MEMBER, new Position(3,0));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,0).y, player.position.y);
    }

    @Test
    void FalseMoveUpBlueTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(3,4));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,4).y, player.position.y);
    }

    @Test
    void MovDownTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(3,13));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,14).y, player.position.y);
    }

    @Test
    void FalseMoveDownTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(3,15));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,15).y, player.position.y);
    }

    @Test
    void FalseMoveDownRedTest()
    {
        Player player = new Player(TeamColor.RED, TeamRole.MEMBER, new Position(3,11));
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,11).y, player.position.y);
    }


}