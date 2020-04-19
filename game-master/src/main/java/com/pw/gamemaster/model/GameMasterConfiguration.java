package com.pw.gamemaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;

@Data
@NoArgsConstructor
public class GameMasterConfiguration {
    public double shamProbability;
    public int maxTeamSize;
    public int maxPieces;
    public int initialPieces;
    public Point[] predefinedGoalPositions;
    public int boardWidth;
    public int boardTaskHeight;
    public int boardGoalHeight;
    public int delayDestroyPiece;
    public int delayNextPieceplace;
    public int delayMove;
    public int delayDiscover;
    public int delayTest;
    public int delayPick;
    public int delayPlace;


    public GameMasterConfiguration(double shamProbability, int maxTeamSize, int maxPieces, int initialPieces, Point[] predefinedGoalPositions,
                                   int boardWidth, int boardTaskHeight, int boardGoalHeight, int delayDestroyPiece, int delayNextPieceplace,
                                   int delayMove, int delayDiscover, int delayTest, int delayPick, int delayPlace) {

        this.shamProbability = shamProbability;
        this.maxTeamSize = maxTeamSize;
        this.maxPieces = maxPieces;
        this.initialPieces = initialPieces;
        this.predefinedGoalPositions = predefinedGoalPositions;
        this.boardWidth = boardWidth;
        this.boardTaskHeight = boardTaskHeight;
        this.boardGoalHeight = boardGoalHeight;
        this.delayDestroyPiece = delayDestroyPiece;
        this.delayNextPieceplace = delayNextPieceplace;
        this.delayMove = delayMove;
        this.delayDiscover = delayDiscover;
        this.delayTest = delayTest;
        this.delayPick = delayPick;
        this.delayPlace = delayPlace;
    }
}
