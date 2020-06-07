package com.pw.player.model;

import com.pw.common.model.*;
import com.pw.player.model.Player;
import com.pw.player.SimpleClient;
import com.pw.server.network.CommunicationServer;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    private int port = 1300;
    private String host = "localhost";

    @Mock
    private SimpleClient client;

    @Before
    public void prepare() throws Exception
    {
        initMocks(this);
        doNothing().when(client).startConnection(host, port);
    }
    
    @Test
    public void StartMessageTest() throws Exception
    {
    	Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(6,4), client);
    	JSONObject tester = new JSONObject();
    	JSONObject position = new JSONObject();
    	JSONObject board = new JSONObject();
    	position.put("x", 6);
    	position.put("y", 4);
    	board.put("boardWidth", 10);
    	board.put("taskAreaHeight", 8);
    	board.put("goalAreaHeight", 4);
        tester.put("action", "start");
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("team", "Blue");
        tester.put("teamRole", TeamRole.MEMBER.toString());
        tester.put("teamSize", (int)4);
        tester.put("position", position);
        tester.put("board", board);
        doReturn(tester.toJSONString()).when(client).receiveMessage();
        player.listen();
        //assertEquals(new Board(10, 4, 8), player.board);
        assertEquals(player.board.boardHeight, 16);
        assertEquals(player.board.boardWidth, 10);
    }
    
    @Test
    public void Discover() throws Exception
    {
    	
    }

    @Test
    public void PickupPieceTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(6,4), client);
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.PIECE);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.PICKUP.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "OK");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.PICKUP, null);
        assertEquals(true, player.piece);
    }


    @Test
    public void PickupPieceTestEmpty() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(6,6), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.PICKUP.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

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
    public void PlacePieceTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(2,2), client);
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.GOAL);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.PLACE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.PLACE, null);
        assertEquals(false, player.piece);
        assertEquals(false, player.tested);
    }

    @Test
    public void MoveRightTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(4,6), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "OK");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.RIGHT);
        assertEquals(new Position(5,6).x, player.position.x);
    }

    @Test
    public void FalseMoveRightTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(9,6), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.RIGHT);
        assertEquals(new Position(9,6).x, player.position.x);
    }

    @Test
    public void MoveLeftTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(4,6), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "OK");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.LEFT);
        assertEquals(new Position(3,6).x, player.position.x);
    }

    @Test
    public void FalseMoveLeftTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(0,6), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.LEFT);
        assertEquals(new Position(0,6).x, player.position.x);
    }

    @Test
    public void MoveUpTest() throws Exception
    {
        Player player = new Player(TeamColor.Red, TeamRole.MEMBER, new Position(3,6), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "OK");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,5).y, player.position.y);
    }

    @Test
    public void FalseMoveUpTest() throws Exception
    {
        Player player = new Player(TeamColor.Red, TeamRole.MEMBER, new Position(3,0), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,0).y, player.position.y);
    }

    @Test
    public void FalseMoveUpBlueTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(3,4), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.UP);
        assertEquals(new Position(3,4).y, player.position.y);
    }

    @Test
    public void MovDownTest() throws Exception
    {
        Player player = new Player(TeamColor.Red, TeamRole.MEMBER, new Position(3,13), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "OK");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,14).y, player.position.y);
    }

    @Test
    public void FalseMoveDownTest() throws Exception
    {
        Player player = new Player(TeamColor.Blue, TeamRole.MEMBER, new Position(3,15), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,15).y, player.position.y);
    }

    @Test
    public void FalseMoveDownRedTest() throws Exception
    {
        Player player = new Player(TeamColor.Red, TeamRole.MEMBER, new Position(3,11), client);
        player.board = new Board(10, 4, 8);

        JSONObject tester = new JSONObject();
        tester.put("action", ActionType.MOVE.toString());
        tester.put("playerGuid", player.playerGuid.toString());
        tester.put("status", "DENIED");
        doReturn(tester.toJSONString()).when(client).receiveMessage();

        player.testAction(ActionType.MOVE, Position.Direction.DOWN);
        assertEquals(new Position(3,11).y, player.position.y);
    }


}