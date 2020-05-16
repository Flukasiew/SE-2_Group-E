package com.pw.player.model;

import com.pw.common.model.*;
import com.pw.player.model.Player;
import com.pw.player.SimpleClient;
import com.pw.server.network.CommunicationServer;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PlayerTest {

    @Mock
    private SimpleClient client;

    @Before
    public void prepare() throws Exception
    {
        initMocks(this);
        doNothing().when(client).startConnection(any(), any());
    }

    @Test
    public void PickupPieceTest() throws Exception
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(6,4), client);
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.PIECE);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(true, player.piece);
    }


    @Test
    public void PickupPieceTestEmpty()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(6,6), client);
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
    public void PlacePieceTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(2,2), client);
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.GOAL);
        player.testAction(ActionType.PLACE, null);
        assertEquals(false, player.piece);
        assertEquals(false, player.tested);
    }

    @Test
    public void MoveRightTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(4,6), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.RIGHT);
        assertEquals(new Position(5,6).x, player.position.x);
    }

    @Test
    public void FalseMoveRightTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(9,6), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.RIGHT);
        assertEquals(new Position(9,6).x, player.position.x);
    }

    @Test
    public void MoveLeftTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(4,6), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.LEFT);
        assertEquals(new Position(3,6).x, player.position.x);
    }

    @Test
    public void FalseMoveLeftTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(0,6), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.LEFT);
        assertEquals(new Position(0,6).x, player.position.x);
    }

    @Test
    public void MoveUpTest()
    {
        Player player = new Player(TeamColor.RED, TeamRole.MEMBER, new Position(3,6), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,5).y, player.position.y);
    }

    @Test
    public void FalseMoveUpTest()
    {
        Player player = new Player(TeamColor.RED, TeamRole.MEMBER, new Position(3,0), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,0).y, player.position.y);
    }

    @Test
    public void FalseMoveUpBlueTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(3,4), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,4).y, player.position.y);
    }

    @Test
    public void MovDownTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(3,13), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,14).y, player.position.y);
    }

    @Test
    public void FalseMoveDownTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(3,15), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,15).y, player.position.y);
    }

    @Test
    public void FalseMoveDownRedTest()
    {
        Player player = new Player(TeamColor.RED, TeamRole.MEMBER, new Position(3,11), client);
        player.board = new Board(10, 4, 8);
        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,11).y, player.position.y);
    }


}