package com.pw.common.dto;

import com.pw.common.model.ActionType;
import com.pw.common.model.Position;
import com.pw.common.model.TeamColor;
import com.pw.common.model.TeamRole;

import javax.swing.*;
import java.util.UUID;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
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

    public Position getPosition(){
        return playerPosition;
    }

    public void setPosition(Position position){
        this.playerPosition = position;
    }

    public TeamColor getPlayerTeamColor() {
        return playerTeamColor;
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

    public void setPlayerTeamColor(TeamColor playerTeamColor) {
        this.playerTeamColor = playerTeamColor;
    }

    public UUID getPlayerGuid(){
        return this.playerGuid;
    }

    public void setPlayerGuid(UUID playerGuid) {
        this.playerGuid = playerGuid;
    }
}