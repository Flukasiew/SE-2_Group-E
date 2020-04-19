package com.pw.common.dto;

import com.pw.common.model.ActionType;
import com.pw.common.model.Position;
import com.pw.common.model.TeamColor;
import com.pw.common.model.TeamRole;
import lombok.Data;

import java.util.UUID;

@Data
public class PlayerDTO {
    public UUID playerGuid;
    public TeamRole playerTeamRole;
    public TeamColor playerTeamColor;
    public ActionType playerAction;
    public Position playerPosition;

    public PlayerDTO() {}

    public PlayerDTO(UUID uuid, ActionType move) {

        this.playerGuid = uuid;
        this.playerAction = move;
    }

    public PlayerDTO(UUID playerGuid, TeamRole playerTeamRole, TeamColor playerTeamColor, ActionType playerAction, Position playerPosition) {
        this.playerGuid = playerGuid;
        this.playerTeamRole = playerTeamRole;
        this.playerTeamColor = playerTeamColor;
        this.playerAction = playerAction;
        this.playerPosition = playerPosition;
    }

    public PlayerDTO(UUID playerGuid, TeamRole playerTeamRole, ActionType playerAction) {
        this.playerGuid = playerGuid;
        this.playerTeamRole = playerTeamRole;
        this.playerAction = playerAction;
    }
}
