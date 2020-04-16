package com.pw.gamemaster.model;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public class GameMaster {
    private int portNumber;
    private InetAddress ipAddress;
    public GameMasterBoard board;
    public GameMasterStatus status;
    private GameMasterConfiguration configuration;
    private int blueTeamGoals;
    private int redTeamGoals;
    private List<UUID> teamRedGuids;
    private List<UUID> teamBlueGuids;

    public void startGame() {

    }

    private void listen() {

    }

    public GameMasterConfiguration loadConfigurationFromJson(String path) {
        throw new RuntimeException("not implemented");
    }

    public void saveConfigurationToJson(String path) {

    }

    private void putNewPiece() {

    }

    private void printBoard() {

    }

    // return type not specified in specifiaction
    public void messageHandler(String message) {

    }
}
