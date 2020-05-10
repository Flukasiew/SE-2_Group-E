package com.pw.player.model;

import com.pw.common.model.*;
import com.pw.player.SimpleClient;

import java.net.InetAddress;
import java.util.UUID;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Player {
    public Team team;

    public String playerName;
    private UUID playerGuid;
    private PlayerState playerState;

    public ActionType lastAction;
    public Position.Direction lastDirection;
    public Position position;
    public Board board;

    public boolean piece;
    public boolean tested;

    private InetAddress ipAddress;
    private int portNumber; // we can think about using InetSocketAddress from java.net
    private int port = 1300;
    private String host = "localhost";
    private SimpleClient client;
    //private SimpleClient simpleClient;

    public Player(TeamColor color, TeamRole role, Position position)
    {
        team = new Team();
        team.setColor(color);
        team.setRole(role);

        this.playerName = "Anon";
        this.playerGuid = UUID.randomUUID();
        this.position = position;

        piece = false;
        tested = false;

        playerState = PlayerState.INITIALIZING;
    }

    public void listen()
    {

    }

    public void startComm()
    {
        try {
            client = new SimpleClient();
            client.startConnection(host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeAction()
    {
        if(piece == true && tested == false)
            testPiece();
        else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.GOAL)
            placePiece();
        else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.PIECE)
            takePiece();
        else if(howManyUnknown() >= 5)
            discover();
        else if(piece == true && tested == true ||
                piece == false)
            move(chooseDirection());
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

    private Position.Direction chooseDirection()
    {
        int cellToMove[] = {-1, 1};

        if(piece == true && tested == true)
            cellToMove = findClosestGoal();
        else if(findPiece()[0] != -1)
            cellToMove = findPiece();
        else
        {
            cellToMove[0] = 0 + (int)(Math.random() * ((board.boardWidth - 0) + 1));
            cellToMove[1] = board.goalAreaHeight + (int)(Math.random() * (((board.goalAreaHeight + board.taskAreaHeight) - board.goalAreaHeight) + 1));
        }

        if(cellToMove[0] > position.x)
            return Position.Direction.RIGHT;
        else if(cellToMove[0] > position.x)
            return Position.Direction.LEFT;
        else if(cellToMove[1] > position.y)
            return Position.Direction.DOWN;
        else
            return Position.Direction.UP;
    }

    private int[] findPiece()
    {
        int[] pieceCell = {-1, -1};
        int x = -1, y = -1;

        for(x = 0; x < board.boardWidth; ++x)
            for(y = board.taskAreaHeight; y < board.goalAreaHeight + board.taskAreaHeight; ++y)
                if(board.cellsGrid[x][y].cellState == Cell.CellState.GOAL)
                {
                    pieceCell[0] = x;
                    pieceCell[1] = y;
                    return pieceCell;
                }

        return pieceCell;
    }

    private int[] findClosestGoal()
    {
        int[] goalCell = {-1, -1};
        int x = -1, y = -1;

        if(team.color == TeamColor.BLUE)
            for(x = 0; x < board.boardWidth; ++x)
                for(y = board.taskAreaHeight + board.goalAreaHeight; y < board.taskAreaHeight + 2 * board.goalAreaHeight; ++y)
                    if(board.cellsGrid[x][y].cellState == Cell.CellState.GOAL)
                        break;
                    else
                        for(x = 0; x < board.boardWidth; ++x)
                            for(y = 0; y < board.taskAreaHeight; ++y)
                                if(board.cellsGrid[x][y].cellState == Cell.CellState.GOAL)
                                    break;

        goalCell[0] = x;
        goalCell[1] = y;
        return  goalCell;
    }

    private void singleDiscover(int x, int y)
    {
        if (board.cellsGrid[x][y].getCellState() == Cell.CellState.UNKNOWN &&
                x >= 0 && x < board.boardWidth && y >= 0  && y < board.boardHeight)
        {
            //board.cellsGrid[x][y].setCellState("Game Master returns");
        }
    }

    private boolean CanMove(Position.Direction dir)
    {
        int x = position.x;
        int y = position.y;

        if(x<=0&&dir==Position.Direction.LEFT)
            return false;
        if(x>=board.boardWidth-1&&dir==Position.Direction.RIGHT)
            return false;
        if(y<=0&&dir==Position.Direction.UP)
            return false;
        if(y>=board.boardHeight-1&&dir==Position.Direction.DOWN)
            return false;
        if(team.color == TeamColor.RED&&y>=board.taskAreaHeight+board.goalAreaHeight-1&&dir==Position.Direction.DOWN)
            return false;
        if(team.color == TeamColor.BLUE&&y<=board.goalAreaHeight&&dir==Position.Direction.UP)
            return false;
        return true;
    }

    private void discover()
    {
        int x = position.x;
        int y = position.y;
        JSONObject message = new JSONObject();
        message.put("action", ActionType.DISCOVER);
        message.put("playerGuid",playerGuid);
        JSONObject response = sendMessage(message);
        //JSONParser parser = new JSONParser();
        //message = (JSONObject)parser.parse(response);
        String stat = (String)response.get("status");
        String res = (String)response.get("test");
        if(stat=="OK")
        {
            if(res=="")
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

    private void move(Position.Direction direction)
    {
        JSONObject message = new JSONObject();
        message.put("action", ActionType.MOVE);
        message.put("playerGuid",playerGuid);
        message.put("direction", direction);
        JSONObject response = sendMessage(message);
        //JSONParser parser = new JSONParser();
        //message = (JSONObject)parser.parse(response);
        String stat = (String)response.get("status");
        if(stat=="OK")
        {
            Position oldPosition;
            oldPosition = position;
            position.changePosition(direction);

            if (CanMove(direction))
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
        JSONObject response = sendMessage(message);
        //JSONParser parser = new JSONParser();
        //message = (JSONObject)parser.parse(response);
        String stat = (String)response.get("status");
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
        JSONObject response = sendMessage(message);
        //JSONParser parser = new JSONParser();
        //message = (JSONObject)parser.parse(response);
        String stat = (String)response.get("status");
        String res = (String)response.get("test");
        if(stat=="OK")
            if(res=="false")
            {
                lastAction = ActionType.DESTROY;
                piece = false;
                tested = false;
            }
            else if(res=="true")
            {
                lastAction = ActionType.TEST;
                tested = true;
                return;
            }
            else return;
        else return;
    }

    private void placePiece()
    {
        JSONObject message = new JSONObject();
        message.put("action", ActionType.PLACE);
        message.put("playerGuid",playerGuid);
        JSONObject response = sendMessage(message);
        //JSONParser parser = new JSONParser();
        //message = (JSONObject)parser.parse(response);
        String stat = (String)response.get("status");
        String res = (String)response.get("test");
        if(stat=="OK")
        {
            if(res=="correct")
            {
                lastAction = ActionType.PLACE;
                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.GOAL);
                piece = false;
                tested = false;
            }
            else
            {
                lastAction = ActionType.PLACE;
                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
                piece = false;
                tested = false;
            }
        }
        else return;
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
            case PLACE:
                placePiece();
                break;
        }
    }

    private JSONObject sendMessage(JSONObject message)
    {
        ActionType action = (ActionType)message.get("action");
        JSONObject response = new JSONObject();
        switch(action)
        {
            case TEST:
                response.put("action", ActionType.TEST);
                response.put("playerGuid", playerGuid);
                if(piece == true)
                {
                    response.put("status", "OK");
                    response.put("test", true);
                }
                else if(piece == false)
                {
                    response.put("status", "DENIED");
                    response.put("test", null);
                }
                break;
            case PICKUP:
                response.put("action", ActionType.PICKUP);
                response.put("playerGuid", playerGuid);
                if(board.cellsGrid[position.x][position.y].getCellState()==Cell.CellState.PIECE)
                    response.put("status", "OK");
                else
                    response.put("status", "DENIED");
                break;
            case PLACE:
                response.put("action", ActionType.PICKUP);
                response.put("playerGuid", playerGuid);
                if(board.cellsGrid[position.x][position.y].getCellState()==Cell.CellState.GOAL)
                {
                    response.put("status", "OK");
                    response.put("placementResult", "Correct");
                }
                else
                {
                    response.put("status","OK");
                    response.put("placementResult","Pointless");
                }
                break;
            case MOVE:
                response.put("action", ActionType.MOVE);
                response.put("playerGuid", playerGuid);
                if(CanMove((Position.Direction)message.get("direction")))
                {
                    response.put("status", "OK");
                }
                else
                {
                    response.put("status", "DENIED");
                }
        }
        return response;
    }
}
