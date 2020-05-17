package com.pw.player.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.PlayerConnectMessageDTO;
import com.pw.common.model.*;
import com.pw.player.SimpleClient;
import javax.management.RuntimeErrorException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import javax.net.ssl.SSLEngineResult.Status;

import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player {
	private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
    public Team team;

    public String playerName;
    public UUID playerGuid;
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
    private String host = "0.0.0.0";
    private SimpleClient client;
    private boolean on = false;

    public Player(TeamColor color, TeamRole role, Position position, SimpleClient client)
    {
        team = new Team();
        team.setColor(color);
        team.setRole(role);

        this.client = client;

        this.playerName = "Anon";
        this.playerGuid = UUID.randomUUID();
        this.position = position;

        piece = false;
        tested = false;

        playerState = PlayerState.INITIALIZING;
        LOGGER.info("Player initialized");
        playerState = PlayerState.ACTIVE;

        startComm();
        listen();
    }
    
    public Player()
    {
    	this.playerName = "Anon";
    	this.playerGuid = UUID.randomUUID();

    	piece = false;
    	tested = false;

    	this.client = new SimpleClient();

    	playerState = PlayerState.INITIALIZING;
        LOGGER.info("Player initialized");
        playerState = PlayerState.ACTIVE;

        startComm();
    	listen();
    }

    public void listen()
    {
    	try {
            while (true) {
                String msg = client.receiveMessage();
                LOGGER.info(msg);
                if(!msg.isEmpty())
                {
                	JSONParser parser = new JSONParser();
                    JSONObject object = (JSONObject)parser.parse(msg);
                    if(object.get("action")=="end")
                    {
                    	on = false;
                    	if(team.getColor()==(TeamColor)object.get("result"))
                    	{
                    		LOGGER.info("We won!");
                    	}
                    	else
                    		LOGGER.info("We lost :(");
                    	break;
                    }
                    else if(object.get("action")=="ready")
                    {
                    	LOGGER.info("Received ready message");
                    	object.put("status", "YES");
                    	LOGGER.info("Sending ready status message");
                    	client.sendMessage(object.toJSONString());
                    }
                    else if(object.get("action")=="start")
                    {
                    	LOGGER.info("Received start message");
                    	team = new Team();
                    	team.setColor((TeamColor)object.get("team"));
                    	team.setRole((TeamRole)object.get("teamRole"));
                    	team.size = (int)object.get("teamSize");
                    	position = (Position)object.get("position");
                    	board = (Board)object.get("board");
                    	on = true;
                    }
                }
                else if(on)
                {
                	makeAction();
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Exception occured when listening", e.toString());
        }
    }

    public void startComm()
    {
        try {
        	LOGGER.info("Connecting player");
            client.startConnection(host, port);
            client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, playerGuid.toString())));
            LOGGER.info("Player connected");
        } catch (Exception e) {
        	LOGGER.error(e.toString());
            e.printStackTrace();
        }
    }

    public void makeAction()
    {
    	LOGGER.info("Choosing action");
        if(piece == true && tested == false)
        	try {
        		testPiece();
        	} catch (Exception e) {
        		LOGGER.error(e.toString());
        		e.printStackTrace();
        	}
        else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.GOAL)
            try{
            	placePiece();
            } catch (Exception e) {
            	LOGGER.error(e.toString());
            	e.printStackTrace();
            }
        else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.PIECE)
            try {
            	takePiece();
            } catch (Exception e) {
            	LOGGER.error(e.toString());
            	e.printStackTrace();
            }
        else if(howManyUnknown() >= 5)
        	try {
        		discover();
        	} catch (Exception e) {
        		LOGGER.error(e.toString());
        		e.printStackTrace();
        	}
        else if(piece == true && tested == true ||
                piece == false)
            try {
            	move(chooseDirection());
            } catch (Exception e) {
            	LOGGER.error(e.toString());
	            e.printStackTrace();
	        }
        else
        	try {
        		discover();
        	} catch (Exception e) {
        		LOGGER.error(e.toString());
        		e.printStackTrace();
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

    private Position.Direction chooseDirection()
    {
    	LOGGER.info("Player chooses direction");
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
        {
        	LOGGER.info("Cannot move left");
            return false;
        }
        if(x>=board.boardWidth-1&&dir==Position.Direction.RIGHT)
        {
        	LOGGER.info("Cannot move right");
            return false;
        }
        if(y<=0&&dir==Position.Direction.UP)
        {
        	LOGGER.info("Cannot move up");
            return false;
        }
        if(y>=board.boardHeight-1&&dir==Position.Direction.DOWN)
        {
        	LOGGER.info("Cannot move down");
            return false;
        }
        if(team.color == TeamColor.RED&&y>=board.taskAreaHeight+board.goalAreaHeight-1&&dir==Position.Direction.DOWN)
        {
        	LOGGER.info("Approached blue goal area, cannot move down");
            return false;
        }
        if(team.color == TeamColor.BLUE&&y<=board.goalAreaHeight&&dir==Position.Direction.UP)
        {
        	LOGGER.info("Approached red goal area, cannot move down");
            return false;
        }
        return true;
    }

    private void discover() throws IOException, ParseException {
	    {
	    	LOGGER.info("Discovering");
	        int x = position.x;
	        int y = position.y;
	        
	        JSONObject message = new JSONObject();
	        message.put("action", ActionType.DISCOVER);
	        message.put("playerGuid",playerGuid);
	        message.put("position", position);
	        client.sendMessage(message.toJSONString());
	        
	        JSONParser parser = new JSONParser();
	        JSONObject response = (JSONObject)parser.parse(client.receiveMessage());
	        String stat = (String)response.get("status");
	        if(stat.equals("OK"))
	        {
	        	List<Field> fields = new ArrayList<>();
	        	//ObjectMapper mapper = new ObjectMapper();
	        	fields = (List<Field>)response.get("fields");
	            for(Field f : fields)
	            {
	            	board.cellsGrid[f.getPosition().x][f.getPosition().y].setCellState(f.cell.getCellState());
	            }
	            //lastAction = ActionType.DISCOVER;
	        }
	        LOGGER.info("Discovered");
	    }
    }

    private void move(Position.Direction direction) throws ParseException, IOException{
	    {
	    	LOGGER.info("Moving");
	        JSONObject message = new JSONObject();
	        message.put("action", ActionType.MOVE);
	        message.put("playerGuid",playerGuid);
	        message.put("direction", direction);
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        JSONObject response = (JSONObject)parser.parse(client.receiveMessage());
	        //JSONParser parser = new JSONParser();
	        //message = (JSONObject)parser.parse(response);
	        String stat = (String)response.get("status");
	        if(stat.equals("OK"))
	        {
	            Position oldPosition;
	            oldPosition = position;
	            position.changePosition(direction);
	
	            if (CanMove(direction))
	                lastAction = ActionType.MOVE;
	            else
	                position = oldPosition;
	        }
	        LOGGER.info("Moved");
	    }
    }

    private void takePiece() throws IOException, ParseException {
	    {
	    	LOGGER.info("Picking up piece");
	        lastAction = ActionType.PICKUP;
	        JSONObject message = new JSONObject();
	        message.put("action", ActionType.PICKUP);
	        message.put("playerGuid",playerGuid);
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        JSONObject response = (JSONObject)parser.parse(client.receiveMessage());
	        String stat = (String)response.get("status");
	        if(stat.equals("OK"))
	        {
	            board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
	            piece = true;
	            LOGGER.info("Picked up piece");
	        }
	        else
	        {
	        	LOGGER.info("Failed to pick up piece");
	            return;
	        }
	    }
    }

    private void testPiece() throws IOException, ParseException {
	    {
	    	LOGGER.info("Testing piece");
	        JSONObject message = new JSONObject();
	        message.put("action", ActionType.TEST);
	        message.put("playerGuid",playerGuid);
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        JSONObject response = (JSONObject)parser.parse(client.receiveMessage());
	        String stat = (String)response.get("status");
	        String res = (String)response.get("test");
	        if(stat.equals("OK"))
	            if(res=="false")
	            {
	            	LOGGER.info("Piece is a sham");
	                lastAction = ActionType.DESTROY;
	                piece = false;
	                tested = false;
	            }
	            else if(res=="true")
	            {
	            	LOGGER.info("Piece is good");
	                lastAction = ActionType.TEST;
	                tested = true;
	                return;
	            }
	            else return;
	        else
	        {
	        	LOGGER.info("Cannot test");
	        	return;
	        }
	    }
    }

    private void placePiece() throws IOException, ParseException {
	    {
	    	LOGGER.info("Placing piece");
	        JSONObject message = new JSONObject();
	        message.put("action", ActionType.PLACE);
	        message.put("playerGuid",playerGuid);
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        JSONObject response = (JSONObject)parser.parse(client.receiveMessage());
	        String stat = (String)response.get("status");
	        String res = (String)response.get("test");
	        if(stat.equals("OK"))
	        {
	            if(res=="correct")
	            {
	                lastAction = ActionType.PLACE;
	                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.GOAL);
	                piece = false;
	                tested = false;
	                LOGGER.info("Goal completed");
	            }
	            else
	            {
	                lastAction = ActionType.PLACE;
	                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
	                piece = false;
	                tested = false;
	                LOGGER.info("Piece wasted");
	            }
	        }
	        else 
	        {
	        	LOGGER.info("Cannot place piece");
	        	return;
	        }
	    }
    }

    public void testAction(ActionType action, Position.Direction direction)
    {
        switch(action)
        {
            case TEST:
            	try {
            		testPiece();
            	} catch (Exception e) {
            		LOGGER.error(e.toString());
            		e.printStackTrace();
            	}
                break;
            case MOVE:
            	try {
            		move(direction);
            	} catch (Exception e) {
            		LOGGER.error(e.toString());
            		e.printStackTrace();
            	}
            	break;
            case PICKUP:
            	try {
            		takePiece();
            	} catch (Exception e) {
            		LOGGER.error(e.toString());
            		e.printStackTrace();
            	}
                break;
            case PLACE:
            	try {
            		placePiece();
            	} catch (Exception e) {
            		LOGGER.error(e.toString());
            		e.printStackTrace();
            	}
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
    
    private JSONObject messenger(String message) throws ParseException, IOException {
	    {
	    	JSONParser jsonParser = new JSONParser();
	        JSONObject msg = (JSONObject)jsonParser.parse(message);
	        String action = (String)msg.get("action");
	        switch(action)
	        {
	        case "setup":
	        	msg.put("status", "OK");
	        	break;
	        case "connect":
	        	msg.put("status", "OK");
	        	break;
	        case "ready":
	        	msg.put("status", "YES");
	        	break;
	        default:
	        	break;
	        }
	        return msg;
	    }
    }
}
