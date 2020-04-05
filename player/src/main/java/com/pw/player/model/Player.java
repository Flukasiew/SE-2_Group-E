package com.pw.player.model;

public class Player {
    public enum ActionType {
        MOVE,
        PICKUP,
        TEST,
        PLACE,
        DESTROY,
        SEND
    }

    public enum PlayerState {
        INITIALIZING,
        ACTIVE,
        COMPLETED
    }
}
