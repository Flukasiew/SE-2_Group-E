package com.pw.player.model;

import com.pw.common.Position;

import java.util.UUID;

public class PlayerDto {
    public UUID playerGuid;
    public Team.TeamRole playerTeamRole;
    public Team.TeamColor playerTeamColor;
    public Player.ActionType playerAction;
    public Position playerPosition;

    public PlayerDto() {

    }
}
