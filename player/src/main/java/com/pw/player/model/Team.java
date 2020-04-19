package com.pw.player.model;

import com.pw.common.model.TeamColor;
import com.pw.common.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Team {
    public TeamColor color;
    public TeamRole role;
    public int size;

    public boolean isLeader() {
        return role == TeamRole.LEADER;
    }

    public TeamColor getColor()
    {
        return color;
    }

    public void setColor(TeamColor color)
    {
        this.color = color;
    }

    public void setRole(TeamRole role)
    {
        this.role = role;
    }

    public TeamRole getRole()
    {
        return role;
    }
}
