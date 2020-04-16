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

public class GameMasterBoard extends Board {
    private Set<Position> piecesPosition;
    public HashSet<UUID> piecesTested;

    public GameMasterBoard() {
        super();
    }

    public Position playerMove(PlayerDTO playerDTO, Position.Direction direction) {
        throw new RuntimeException("not implemented");
    }

    public Cell.CellState takePiece(Position position) {

        throw new RuntimeException("not implemented");
    }

    public Position generatePiece(double chance) {

        throw new RuntimeException("not implemented");
    }

    public void setGoal(Position position) {

    }

    public PlacementResult placePiece(PlayerDTO playerDTO) {

        throw new RuntimeException("not implemented");
    }

    public Position placePlayer(PlayerDTO playerDTO) {

        throw new RuntimeException("not implemented");
    }

    public boolean checkWinCondition(TeamColor teamColor) {

        throw new RuntimeException("not implemented");
    }

    public List<Field> discover(Position position) {

        throw new RuntimeException("not implemented");
    }

    public int manhattanDistanceTwoPoints(Point pointA, Point pointB) {

        throw new RuntimeException("not implemented");
    }
}
