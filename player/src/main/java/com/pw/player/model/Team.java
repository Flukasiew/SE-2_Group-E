package com.pw.player.model;

import lombok.Data;

@Data
public class Team {
    public TeamColor color;
    public TeamRole role;
    public int size;


    public enum TeamRole {
        LEADER,
        MEMBER
    }

    public enum TeamColor {
        RED,
        BLUE
    }

    public Team(TeamRole teamRole, TeamColor teamColor, int size) {
        this.color = teamColor;
        this.size = size;
        this.role = teamRole;
    }

    public boolean isLeader() {

        return role == TeamRole.LEADER;
    }
}
