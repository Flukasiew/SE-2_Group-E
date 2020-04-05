package com.pw.player.model;

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

    public TeamColor getColor() {
        return color;
    }

    public void setColor(TeamColor teamColor) {
        this.color = teamColor;
    }

    public TeamRole getRole() {
        return role;
    }

    public void setRole(TeamRole teamRole) {
        this.role = teamRole;
    }

    public boolean isLeader() {
        return role == TeamRole.LEADER;
    }
}
