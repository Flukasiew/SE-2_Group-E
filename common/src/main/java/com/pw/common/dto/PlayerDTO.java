package com.pw.common.dto;

import com.pw.common.model.ActionType;
import com.pw.common.model.Position;
import com.pw.common.model.TeamColor;
import com.pw.common.model.TeamRole;

import java.util.UUID;

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
}
