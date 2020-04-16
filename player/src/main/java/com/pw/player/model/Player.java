package com.pw.player.model;

import com.pw.common.model.ActionType;
import com.pw.common.model.Board;
import com.pw.common.model.Position;

import java.net.InetAddress;
import java.util.UUID;

public class Player {
    public Team team;
    private InetAddress ipAddress;
    private int portNumber; // we can think about using InetSocketAddress from java.net
    public String playerName;
    public ActionType lastAction;
    public Position.Direction lastDirection;
    public Position position;
    public Board board;
    public boolean piece;
    private UUID playerGuid;
    private PlayerState playerState;

    public void listen() {

    }

    public void makeAction() {

    }

    private void discover() {

    }

    private void move() {

    }

    private void takePiece() {

    }

    private void testPiece() {

    }

    private void placePiece() {

    }
}
