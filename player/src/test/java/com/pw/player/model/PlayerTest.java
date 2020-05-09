package com.pw.player.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.PlayerConnectMessageDTO;
import com.pw.common.dto.PlayerDTO;
import com.pw.common.model.*;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public SimpleClient simpleClient;
    private String host = "localhost";
    private int port = 1300;

    private Player player;

    @Before
    public void prepare() {
        player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(6, 4));
        player.board = new Board(10, 4, 8);
    }

    @Test
    void PickupPieceTest() throws Exception
    {
        simpleClient = new SimpleClient();
        simpleClient.startConnection(host, port);
        simpleClient.receiveMessage();

        simpleClient.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, player.playerGuid)));
        simpleClient.sendMessage(MAPPER.writeValueAsString(new PlayerDTO(player.playerGuid, ActionType.PICKUP)));
        player.listen();

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.PIECE);

        player.testAction(ActionType.PICKUP, null);
        assertEquals(true, player.hasPiece);
    }

    void PickupPieceTestEmpty() throws Exception
    {
        simpleClient = new SimpleClient();
        simpleClient.startConnection(host, port);
        simpleClient.receiveMessage();

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.EMPTY);

        simpleClient.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, player.playerGuid)));
        simpleClient.sendMessage(MAPPER.writeValueAsString(new PlayerDTO(player.playerGuid, ActionType.PICKUP)));
        player.listen();

        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.hasPiece);

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.GOAL);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.hasPiece);

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.VALID);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.hasPiece);

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.UNKNOWN);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.hasPiece);
    }

    void PlacePieceTest() throws Exception
    {
        simpleClient = new SimpleClient();
        simpleClient.startConnection(host, port);
        simpleClient.receiveMessage();

        player.board.cellsGrid[6][4].setCellState(Cell.CellState.GOAL);

        simpleClient.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, player.playerGuid)));
        simpleClient.sendMessage(MAPPER.writeValueAsString(new PlayerDTO(player.playerGuid, ActionType.PLACE)));
        player.listen();

        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.hasPiece);


    }

}