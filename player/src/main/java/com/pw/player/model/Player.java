package com.pw.player.model;

import com.pw.common.model.*;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Player {
    public String playerName;
    public UUID playerGuid;
    public Team team;

    public ActionType lastAction;
    public Position.Direction lastDirection;
    public Position position;
    public Board board;

    public boolean hasPiece;
    public boolean tested;

    private PlayerState playerState;
    public boolean connection;

    public Player(TeamColor color, TeamRole role, Position position)
    {
        Team _team = new Team();
        playerName = "Anon";

        _team.setColor(color);
        _team.setRole(role);

        this.team = _team;
        this.position = position;

        hasPiece = false;
        tested = false;

        playerGuid = UUID.randomUUID();
        playerState = PlayerState.INITIALIZING;
    }

    public void listen()
    {

    }

    public void makeAction()
    {
        if(hasPiece == true && tested == false)
            testPiece();
        else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.GOAL)
            placePiece();
        else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.PIECE)
            takePiece();
        else if(hasPiece == true)
            move(chooseDirection());
        if(howManyUnknown() >= 5)
            discover();
    }

    private Position.Direction chooseDirection()
    {
        return Position.Direction.UP;
    }

    private void singleDiscover(int x, int y)
    {
        if (board.cellsGrid[x][y].getCellState() == Cell.CellState.UNKNOWN &&
                x >= 0 && x < board.boardWidth && y >= 0  && y < board.boardHeight)
        {
            Cell.CellState cellState = Cell.CellState.UNKNOWN;
            board.cellsGrid[x][y].setCellState(cellState);
        }
    }

    private int howManyUnknown()
    {
        int counter = 0;
        int x = position.x;
        int y = position.y;

        if(board.cellsGrid[x][y].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x-1][y].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x-1][y+1].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x-1][y-1].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x+1][y].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x+1][y-1].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x+1][y+1].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x][y+1].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(board.cellsGrid[x][y-1].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;

        return counter;
    }

    private boolean CanMove()
    {
        int x = position.x;
        int y = position.y;

        if (x < 0 && x >= board.boardWidth && y < 0  && y >= board.boardHeight)
            return false;

        if(team.color == TeamColor.BLUE)
        {
            if (y < board.taskAreaHeight)
                return false;
        }

        if(team.color == TeamColor.RED)
        {
            if (y > board.boardHeight - board.taskAreaHeight)
                return false;
        }

        return true;
    }


    private void discover()
    {
        int x = position.x;
        int y = position.y;
        JSONObject message = new JSONObject();
        message.put("action", ActionType.DISCOVER);
        message.put("playerGuid",playerGuid);
        String response = sendMessage(message);
        JSONParser parser = new JSONParser();
        message = (JSONObject)parser.parse(response);
        String stat = (String)message.get("status");
        String res = (String)message.get("test");
        if(stat=="OK")
        {
            singleDiscover(x,y);
            singleDiscover(x-1,y);
            singleDiscover(x-1,y+1);
            singleDiscover(x-1,y-1);
            singleDiscover(x+1,y);
            singleDiscover(x+1,y-1);
            singleDiscover(x+1,y+1);
            singleDiscover(x,y+1);
            singleDiscover(x,y-1);
            //lastAction = ActionType.DISCOVER;
        }
    }

    private void move(Position.Direction direction) {
        JSONObject message = new JSONObject();
        message.put("action", ActionType.MOVE);
        message.put("playerGuid",playerGuid);
        message.put("direction", direction);
        String response = sendMessage(message);
        JSONParser parser = new JSONParser();
        message = (JSONObject)parser.parse(response);
        String stat = (String)message.get("status");
        if(stat=="OK")
        {
            Position oldPosition;
            oldPosition = position;
            position.changePosition(direction);

            if (CanMove())
                lastAction = ActionType.MOVE;
            else
                position = oldPosition;
        }
    }

    private void takePiece()
    {
        lastAction = ActionType.PICKUP;
        JSONObject message = new JSONObject();
        message.put("action", ActionType.PICKUP);
        message.put("playerGuid",playerGuid);
        String response = sendMessage(message);
        JSONParser parser = new JSONParser();
        message = (JSONObject)parser.parse(response);
        String stat = (String)message.get("status");
        if(stat=="OK")
        {
            board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
            piece = true;
        }
        else
            return;
    }

    private void testPiece()
    {
        JSONObject message = new JSONObject();
        message.put("action", ActionType.TEST);
        message.put("playerGuid",playerGuid);
        String response = sendMessage(message);
        JSONParser parser = new JSONParser();
        message = (JSONObject)parser.parse(response);
        String stat = (String)message.get("status");
        String res = (String)message.get("test");
        if(stat=="OK")
            if(res=="false")
            {
                lastAction = ActionType.DESTROY;
                hasPiece = false;
                tested = false;
            }
            else if(res=="true")
            {
                lastAction = ActionType.TEST;
                tested = true;
                return;
            }
            else
                return;
        else
            return;
    }

    private void placePiece()
    {
        JSONObject message = new JSONObject();
        message.put("action", ActionType.TEST);
        message.put("playerGuid",playerGuid);
        String response = sendMessage(message);
        JSONParser parser = new JSONParser();
        message = (JSONObject)parser.parse(response);
        String stat = (String)message.get("status");
        String res = (String)message.get("test");
        if(stat == "OK")
        {
            if(res == "correct")
            {
                lastAction = ActionType.PLACE;
                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.GOAL);
                hasPiece = false;
                tested = false;
            }
            else
            {
                lastAction = ActionType.PLACE;
                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
                hasPiece = false;
                tested = false;
            }
        }
        else
            return;
    }

    public void testAction(ActionType action, Position.Direction direction)
    {
        switch(action)
        {
            case TEST:
                testPiece();
                break;
            case MOVE:
                move(direction);
                break;
            case PICKUP:
                takePiece();
                break;
        }
    }

    private void sendMessage()
    {}
}
