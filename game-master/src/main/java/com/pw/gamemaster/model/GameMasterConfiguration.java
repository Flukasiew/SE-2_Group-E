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
    public long startupTimeLimit;
    public long playersConnectTimeLimit;
    public long gameMsgTimeLimit;

    public GameMasterConfiguration(double shamProbability, int maxTeamSize, int maxPieces, int initialPieces, Point[] predefinedGoalPositions,
                                   int boardWidth, int boardTaskHeight, int boardGoalHeight, int delayDestroyPiece, int delayNextPieceplace,
                                   int delayMove, int delayDiscover, int delayTest, int delayPick, int delayPlace,
                                   long startupTimeLimit, long playersConnectTimeLimit, long gameMsgTimeLimit) {

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
        this.startupTimeLimit = startupTimeLimit*1000;
        this.playersConnectTimeLimit = playersConnectTimeLimit*1000;
        this.gameMsgTimeLimit = gameMsgTimeLimit*1000;
    }

    public boolean checkData() {
        if(shamProbability<0 || shamProbability>1) return false;
        if(maxTeamSize<0 || maxTeamSize>4) return false;
        if(maxPieces<0 || maxPieces>(2*boardGoalHeight+boardTaskHeight)*boardWidth) return false;
        if(initialPieces<0 || initialPieces>maxPieces) return false;
        if(boardWidth<=0) return false;
        if(boardTaskHeight<=0) return false;
        if(boardGoalHeight<=0) return false;
        if(predefinedGoalPositions.length>boardGoalHeight*boardWidth) return false;
        if(startupTimeLimit<0 || playersConnectTimeLimit<0 || gameMsgTimeLimit<0) return false;
        return true;
    }
}
