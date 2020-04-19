package com.pw.common.dto;

import com.pw.common.model.ActionType;
import com.pw.common.model.Position;
import com.pw.common.model.TeamColor;
import com.pw.common.model.TeamRole;

import javax.swing.*;
import java.util.UUID;
import lombok.Data;

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

    public Position getPosition(){
        return playerPosition;
    }

    public void setPosition(Position position){
        this.playerPosition = position;
    }

    public TeamColor getPlayerTeamColor() {
        return playerTeamColor;
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