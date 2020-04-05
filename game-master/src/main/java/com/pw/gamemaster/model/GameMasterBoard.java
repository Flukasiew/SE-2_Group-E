package com.pw.gamemaster.model;

import com.pw.common.Board;
import com.pw.common.Cell;
import com.pw.common.Field;
import com.pw.common.Position;
import com.pw.player.model.Player;
import com.pw.player.model.PlayerDto;
import com.pw.player.model.Team;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameMasterBoard extends Board {
    private Set<Position> piecesPosition;
    public HashSet<UUID> piecesTested;
    // since these values are to be inherited, im not sure if theyre needed here, someone confirm pls
    //    public Cell[][] cellsGrid;
    //    public int goalAreaHeight;
    //    public int taskAreaHeight;
    //    public int boardHeight = 2 * goalAreaHeight + taskAreaHeight;
    //    public int boardWidth;

    public enum PlacementResult {
        CORRECT,
        POINTLESS
    }

    public GameMasterBoard() {
        super();
    }

    public Position playerMove(PlayerDto playerDto, Position.Direction direction) {
        return null;
    }

    public Cell.CellState takePiece(Position position) {
        return null;
    }

    public Position generatePiece(double chance) {
        return null;
    }

    public void setGoal(Position position) {

    }

    public PlacementResult placePiece(PlayerDto playerDto) {
        return null;
    }

    public Position placePlayer(PlayerDto playerDto) {
        return null;
    }

    public boolean checkWinCondition(Team.TeamColor teamColor) {
        return false;
    }

    public List<Field> discover(Position position) {
        return null;
    }

    public int manhattanDistanceTwoPoints(Point pointA, Point pointB) {
        return 0;
    }
}
