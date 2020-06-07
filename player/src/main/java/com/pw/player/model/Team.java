package com.pw.player.model;

import com.pw.common.model.TeamColor;
import com.pw.common.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    
    public void setColor(String color)
    {
    	if(TeamColor.Blue.toString().equals(color))
    		this.color = TeamColor.Blue;
    	else if(TeamColor.Red.toString().equals(color))
    		this.color = TeamColor.Red;
    }
    
    public void setRole(String role)
    {
    	if(TeamRole.MEMBER.toString().equals(role))
    		this.role = TeamRole.MEMBER;
    	else if(TeamRole.LEADER.toString().equals(role))
    		this.role = TeamRole.LEADER;
    }

    public TeamRole getRole()
    {
        return role;
    }
}
