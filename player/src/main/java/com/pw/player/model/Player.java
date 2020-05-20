package com.pw.player.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.PlayerConnectMessageDTO;
import com.pw.common.model.*;
import com.pw.common.model.Cell.CellState;
import com.pw.player.SimpleClient;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;

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
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player {
	private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
    public Team team;
    //private List<Position> positions = new ArrayList<Position>();
    private List<Integer> posx = new ArrayList<Integer>();
    private List<Integer> posy = new ArrayList<Integer>();

    public String playerName;
    public UUID playerGuid;
    private PlayerState playerState;

    public ActionType lastAction;
    public Position.Direction lastDirection;
    public Position position;
    public Board board;

    public boolean piece;
    public boolean tested;
    private int counter=0;

    private InetAddress ipAddress;
    private int portNumber; // we can think about using InetSocketAddress from java.net
    private int port = 1300;
    private String host = "0.0.0.0";
    private SimpleClient client;
    private boolean on = false;
    boolean lastdisc = false;

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
        //listen();
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
    	//listen();
    }

    public void listen()
    {
    	try {
            while (true) {
                String msg = client.receiveMessage();
                //LOGGER.info(msg);
                if(msg!=null && !msg.isEmpty())
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
                    else if(object.get("action").equals("start")&&on==false)
                    {
                    	LOGGER.info("Received start message");
                    	team = new Team();
                    	team.setColor((String)object.get("team"));
                    	team.setRole((String)object.get("teamRole"));
                    	team.size = ((Long)object.get("teamSize")).intValue();
                    	JSONObject newposition = (JSONObject)object.get("position");
                    	position = new Position(((Long)newposition.get("x")).intValue(), ((Long)newposition.get("y")).intValue());
                    	JSONObject newboard = (JSONObject)object.get("board");
                    	board = new Board(((Long)newboard.get("boardWidth")).intValue(), ((Long)newboard.get("goalAreaHeight")).intValue(), ((Long)newboard.get("taskAreaHeight")).intValue());
                    	for(int i=0; i<board.boardHeight; i++)
                    	{
                    		for(int j=0; j<board.boardWidth; j++)
                    		{
                    			board.cellsGrid[j][i].setCellState(CellState.UNKNOWN);
                    		}
                    	}
                    	//board = (Board)object.get("board");
                    	//board = new Board(object.get("board").get("boardWidth"), object.getJSONObject("board").get("goalHeight"));
                    	on = true;
                    }
                    else if(object.get("action").equals("start")&&on==true)
                    	break;
                }
                else if(on)
                {
                	makeAction();
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Exception occured when listening", e.toString());
            LOGGER.info(e.toString());
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
        		lastdisc = !lastdisc;
        	} catch (Exception e) {
        		LOGGER.error(e.toString());
        		e.printStackTrace();
        	}
        //else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.GOAL && piece==true)
        else if(team.color==TeamColor.BLUE&&position.y<board.goalAreaHeight&&board.cellsGrid[position.x][position.y].getCellState()==Cell.CellState.UNKNOWN&&piece==true)
            try{
            	placePiece();
            	lastdisc = !lastdisc;
            } catch (Exception e) {
            	LOGGER.error(e.toString());
            	e.printStackTrace();
            }
        else if(team.color==TeamColor.RED&&position.y>=board.goalAreaHeight+board.taskAreaHeight&&board.cellsGrid[position.x][position.y].getCellState()==Cell.CellState.UNKNOWN&&piece==true)
            try{
            	placePiece();
            	lastdisc = !lastdisc;
            } catch (Exception e) {
            	LOGGER.error(e.toString());
            	e.printStackTrace();
            }
        else if(board.cellsGrid[position.x][position.y].getCellState() == Cell.CellState.PIECE&&piece==false)
            try {
            	takePiece();
            	lastdisc = !lastdisc;
            } catch (Exception e) {
            	LOGGER.error(e.toString());
            	e.printStackTrace();
            }
        else if(team.color==TeamColor.BLUE&&position.y>=board.goalAreaHeight&&lastAction != ActionType.DISCOVER)
        	try {
        		discover();
        		counter++;
        		lastdisc = !lastdisc;
        	} catch (Exception e) {
        		LOGGER.error(e.toString());
        		e.printStackTrace();
        	}
        else if(team.color==TeamColor.RED&&position.y<board.goalAreaHeight+board.taskAreaHeight&&lastAction != ActionType.DISCOVER)
        	try {
        		discover();
        		counter++;
        		lastdisc = !lastdisc;
        	} catch (Exception e) {
        		LOGGER.error(e.toString());
        		e.printStackTrace();
        	}
        else if((piece == true && tested == true) ||
                (piece == false))
            try {
            	//discover();
            	move(chooseDirection());
            	lastdisc = !lastdisc;
            } catch (Exception e) {
            	LOGGER.error(e.toString());
	            e.printStackTrace();
	        }
        else
        	try {
        		discover();
        		counter++;
        		lastdisc = !lastdisc;
        	} catch (Exception e) {
        		LOGGER.error(e.toString());
        		e.printStackTrace();
        	}
    }

    private boolean howManyUnknown()
    {
        int counter = 0;
        int total = 1;
        int x = position.x;
        int y = position.y;

        if(board.cellsGrid[x][y].getCellState() == Cell.CellState.UNKNOWN)
            ++counter;
        if(x>0)
        {
        	total++;
	        if(board.cellsGrid[x-1][y].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }
        if(x>0&&y<board.boardHeight-1)
        {
        	total++;
	        if(board.cellsGrid[x-1][y+1].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }
        if(x>0&&y>0)
        {
        	total++;
	        if(board.cellsGrid[x-1][y-1].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }
        if(x<board.boardWidth-1)
        {
        	total++;
	        if(board.cellsGrid[x+1][y].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }
        if(x<board.boardWidth-1&&y>0)
        {
        	total++;
	        if(board.cellsGrid[x+1][y-1].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }
        if(x<board.boardWidth-1&&y<board.boardHeight-1)
        {
        	total++;
	        if(board.cellsGrid[x+1][y+1].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }
        if(y<board.boardHeight-1)
        {
        	total++;
	        if(board.cellsGrid[x][y+1].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }
        if(y>0)
        {
        	total++;
	        if(board.cellsGrid[x][y-1].getCellState() == Cell.CellState.UNKNOWN)
	            ++counter;
        }

        if(counter>total/2)
        	return true;
        else return false;
    }

    private Position.Direction chooseDirection()
    {
    	LOGGER.info("Player chooses direction");
        int cellToMove[] = {-1, 1};

        if(piece == true && findClosestGoal()[0]!=-1)
        {
            cellToMove = findClosestGoal();
            LOGGER.info("Moving towards goal");
        }
        else if(piece==false&&findPiece()[0] != -1)
        {
        	LOGGER.info("Moving towards piece");
            cellToMove = findPiece();
        }
        else
        {
        	LOGGER.info("Checking distance");
        	cellToMove = minDistance();
        	if(cellToMove[0]==-1)
        	{
        		if(team.color==TeamColor.BLUE)
        		{
        			return Position.Direction.DOWN;
        		}
        		else
        			return Position.Direction.UP;
        	}
        	
            //cellToMove[0] = 0 + (int)(Math.random() * ((board.boardWidth - 0) + 1));
            //cellToMove[1] = board.goalAreaHeight + (int)(Math.random() * (((board.goalAreaHeight + board.taskAreaHeight) - board.goalAreaHeight) + 1));
        }

        if(cellToMove[0] > position.x)
            return Position.Direction.RIGHT;
        else if(cellToMove[0] > position.x)
            return Position.Direction.LEFT;
        else if(team.color == TeamColor.BLUE)
        {
	        if(cellToMove[1] > position.y)
	            return Position.Direction.DOWN;
	        else
	            return Position.Direction.UP;
        }
        else
        {
        	if(cellToMove[1] < position.y)
	            return Position.Direction.UP;
	        else
	            return Position.Direction.DOWN;
        }
    }
    
    private int[] minDistance()
    {
    	int[] pieceCell = {-1, -1};
        int x = -1, y = -1;
        int min = 999;
        if(board.cellsGrid[position.x][position.y].distance<min&&board.cellsGrid[position.x][position.y].distance>=0)
        {
        	min = board.cellsGrid[position.x][position.y].distance;
        	x = position.x;
        	y = position.y;
        }
        if(position.x>0)
	        if(board.cellsGrid[position.x-1][position.y].distance<min&&board.cellsGrid[position.x-1][position.y].distance>=0)
	        {
	        	min = board.cellsGrid[position.x-1][position.y].distance;
	        	x = position.x-1;
	        	y = position.y;
	        }
        if(position.x>0&&position.y>0)
	        if(board.cellsGrid[position.x-1][position.y-1].distance<min&&board.cellsGrid[position.x-1][position.y-1].distance>=0)
	        {
	        	min = board.cellsGrid[position.x-1][position.y-1].distance;
	        	x = position.x-1;
	        	y = position.y-1;
	        }
        if(position.x>0&&position.y<board.boardHeight-1)
        	if(board.cellsGrid[position.x-1][position.y+1].distance<min&&board.cellsGrid[position.x-1][position.y+1].distance>=0)
	        {
	        	min = board.cellsGrid[position.x-1][position.y+1].distance;
	        	x = position.x-1;
	        	y = position.y+1;
	        }
        if(position.x<board.boardWidth-1&&position.y>0)
        	if(board.cellsGrid[position.x+1][position.y-1].distance<min&&board.cellsGrid[position.x+1][position.y-1].distance>=0)
	        {
	        	min = board.cellsGrid[position.x+1][position.y-1].distance;
	        	x = position.x+1;
	        	y = position.y-1;
	        }
        if(position.x<board.boardWidth-1)
        	if(board.cellsGrid[position.x+1][position.y].distance<min&&board.cellsGrid[position.x+1][position.y].distance>=0)
	        {
	        	min = board.cellsGrid[position.x+1][position.y].distance;
	        	x = position.x+1;
	        	y = position.y;
	        }
        if(position.x<board.boardWidth-1&&position.y<board.boardHeight-1)
        	if(board.cellsGrid[position.x+1][position.y+1].distance<min&&board.cellsGrid[position.x+1][position.y+1].distance>=0)
	        {
	        	min = board.cellsGrid[position.x+1][position.y+1].distance;
	        	x = position.x+1;
	        	y = position.y+1;
	        }
        if(position.y>0)
        	if(board.cellsGrid[position.x][position.y-1].distance<min&&board.cellsGrid[position.x][position.y-1].distance>=0)
	        {
	        	min = board.cellsGrid[position.x][position.y-1].distance;
	        	x = position.x;
	        	y = position.y-1;
	        }
        if(position.y<board.boardHeight-1)
        	if(board.cellsGrid[position.x][position.y+1].distance<min&&board.cellsGrid[position.x][position.y+1].distance>=0)
	        {
	        	min = board.cellsGrid[position.x][position.y+1].distance;
	        	x = position.x;
	        	y = position.y+1;
	        }
        LOGGER.info("Minimal distance is " + min);
        pieceCell[0]=x;
        pieceCell[1]=y;
        return pieceCell;
    }

    private int[] findPiece()
    {
        int[] pieceCell = {-1, -1};
        int x = -1, y = -1;

        for(x = 0; x < board.boardWidth; x++)
            for(y = board.goalAreaHeight; y < board.goalAreaHeight + board.taskAreaHeight; y++)
                if(board.cellsGrid[x][y].cellState == Cell.CellState.PIECE)
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
        int x = 0, y = 0;

        if(team.color == TeamColor.BLUE)
        {
        	for(y = 0; y < board.goalAreaHeight; y++)
        	{
        		for(x = 0; x < board.boardWidth; x++)
                    //if(board.cellsGrid[x][y].cellState == Cell.CellState.GOAL)
                	//if(counter<=y*board.boardWidth+x)
        			if(board.cellsGrid[x][y].getCellState() == Cell.CellState.UNKNOWN)
                    {
                    	goalCell[0] = x;
                    	goalCell[1] = y;
                    	return goalCell;
                    }
        	}
        }
        else if(team.color == TeamColor.RED)
        {
        	for(y=board.goalAreaHeight+board.taskAreaHeight; y<board.boardHeight; y++)
        	{
	        	for(x=0; x<board.boardWidth; x++)
	        	{
        			//if(board.cellsGrid[x][y].cellState == Cell.CellState.GOAL)
        			//if(counter<=(y-(board.goalAreaHeight+board.taskAreaHeight))+x)
	        		if(board.cellsGrid[x][y].getCellState() == Cell.CellState.UNKNOWN)
                    {
                    	goalCell[0] = x;
                    	goalCell[1] = y;
                    	return goalCell;
                    }
	        	}
        	}
        }
        return  goalCell;
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
        if(team.color == TeamColor.BLUE&&y>=board.taskAreaHeight+board.goalAreaHeight-1&&dir==Position.Direction.DOWN)
        {
        	LOGGER.info("Approached red goal area, cannot move down");
            return false;
        }
        if(team.color == TeamColor.RED&&y<=board.goalAreaHeight&&dir==Position.Direction.UP)
        {
        	LOGGER.info("Approached blue goal area, cannot move down");
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
	        //JSONObject position = new JSONObject();
	        message.put("action", "discover");
	        message.put("playerGuid",playerGuid.toString());
	        client.sendMessage(message.toJSONString());
	        
	        JSONParser parser = new JSONParser();
	        String read = client.receiveMessage();
	        while(read==null)
	        {
	        	read = client.receiveMessage();
	        }
	        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	        MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	        List<Field> fields = Arrays.asList(MAPPER.readValue(read, Field[].class));
	        //String stat = MAPPER.readValue(read, String.class);
	        JsonNode jsonNode = MAPPER.readTree(read);
	        //JsonNode jsonNode = MAPPER.readValue(read, JsonNode.class);
	        JsonNode statnode = jsonNode.get("status");
	        String stat = statnode.asText();
	        JsonNode fieldnode = jsonNode.get("fields");
	        String field = MAPPER.convertValue(fieldnode, String.class);
	        fields = Arrays.asList(MAPPER.readValue(field, Field[].class));
	        //JsonNode field = fieldnode.get("position");
	        //List<Field> fields = MAPPER.readValue(read, new TypeReference<List<Field>>() {});
	        //JSONObject response = (JSONObject)parser.parse(client.receiveMessage());
	        //String stat = (String)response.get("status");
	        if(stat.equals("OK"))
	        {
	        	posx.add(position.x);
	        	posy.add(position.y);
	        	//List<Field> fields = new ArrayList<>();
	        	//ObjectMapper mapper = new ObjectMapper();
	        	//fields = (List<Field>)response.get("fields");
	            for(Field f : fields)
	            {
	            	if(f.position.y>=board.goalAreaHeight&&f.position.y<board.goalAreaHeight+board.taskAreaHeight)
	            		board.updateField(f);
	            }
	            LOGGER.info("Discovered");
	        }
	        else
	        {
	        	LOGGER.info("Discovering failed");
	        }
	        lastAction = ActionType.DISCOVER;
	    }
    }

    private void move(Position.Direction direction) throws ParseException, IOException{
	    {
	    	LOGGER.info("Moving");
	        JSONObject message = new JSONObject();
	        message.put("action", "move");
	        message.put("playerGuid",playerGuid.toString());
	        message.put("direction", direction.toString());
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        String msg = null;
	        while(msg==null)
	        {
	        	msg = client.receiveMessage();
	        }
	        LOGGER.info("Received move status");
	        JSONObject response = (JSONObject)parser.parse(msg);
	        //JSONParser parser = new JSONParser();
	        //message = (JSONObject)parser.parse(response);
	        String stat = (String)response.get("status");
	        if(stat.equals("OK"))
	        {
	        	if(CanMove(direction))
	        		position.changePosition(direction);
	        }
	        lastAction = ActionType.MOVE;
	        LOGGER.info("Moved");
	    }
    }

    private void takePiece() throws IOException, ParseException {
	    {
	    	LOGGER.info("Picking up piece");
	        lastAction = ActionType.PICKUP;
	        JSONObject message = new JSONObject();
	        message.put("action", "pickup");
	        message.put("playerGuid",playerGuid.toString());
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        String msg = null;
	        while(msg==null)
	        {
	        	msg = client.receiveMessage();
	        }
	        JSONObject response = (JSONObject)parser.parse(msg);
	        String stat = (String)response.get("status");
	        if(stat.equals("OK"))
	        {
	            board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
	            piece = true;
	            LOGGER.info("Picked up piece");
	        }
	        else
	        {
	        	board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
	        	LOGGER.info("Failed to pick up piece");
	            return;
	        }
	    }
    }

    private void testPiece() throws IOException, ParseException {
	    {
	    	LOGGER.info("Testing piece");
	        JSONObject message = new JSONObject();
	        message.put("action", "test");
	        message.put("playerGuid",playerGuid.toString());
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        String msg = null;
	        while(msg==null)
	        {
	        	msg = client.receiveMessage();
	        }
	        JSONObject response = (JSONObject)parser.parse(msg);
	        String stat = (String)response.get("status");
	        boolean res = (boolean)response.get("test");
	        if(stat.equals("OK"))
	            if(res==false)
	            {
	            	LOGGER.info("Piece is a sham");
	                lastAction = ActionType.DESTROY;
	                piece = false;
	                tested = false;
	            }
	            else if(res==true)
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
	        message.put("action", "place");
	        message.put("playerGuid",playerGuid.toString());
	        client.sendMessage(message.toJSONString());
	        JSONParser parser = new JSONParser();
	        String msg = null;
	        while(msg==null)
	        {
	        	msg = client.receiveMessage();
	        }
	        JSONObject response = (JSONObject)parser.parse(msg);
	        String stat = (String)response.get("status");
	        String res = (String)response.get("placementResult");
	        LOGGER.info("Placing at "+position.x+" "+position.y);
	        if(stat.equals("OK"))
	        {
	            if(res=="Correct")
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
