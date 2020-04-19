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

    void PlacePieceTest()
    {
        Player player = new Player(TeamColor.BLUE, TeamRole.MEMBER, new Position(2,2));
        player.board = new Board(10, 4, 8);
        player.board.cellsGrid[6][4].setCellState(Cell.CellState.GOAL);
        player.testAction(ActionType.PICKUP, null);
        assertEquals(false, player.piece);
    }

}