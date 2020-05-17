package com.pw.gamemaster.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameSetupDTO;
import com.pw.common.dto.PlayerDTO;
import com.pw.common.model.*;
import com.pw.common.model.Action;
import com.pw.gamemaster.SimpleClient;
import com.pw.gamemaster.exception.GameSetupException;
import com.pw.gamemaster.exception.PlayerNotConnectedException;
import com.pw.gamemaster.exception.UnexpectedActionException;
import lombok.extern.java.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.RuntimeErrorException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.*;
import java.util.List;


public class GameMaster {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMaster.class);

    public GameMasterBoard board;
    public GameMasterStatus status;
    private int portNumber;
    private InetAddress ipAddress;
    private GameMasterConfiguration configuration;
    private int blueTeamGoals;
    private int redTeamGoals;
    private List<UUID> teamRedGuids;
    private List<UUID> teamBlueGuids;

    private SimpleClient simpleClient;
    private String logFilePath = "log_gm.txt";
    private Dictionary<UUID, PlayerDTO> playersDTO;
    private Map<UUID, Boolean> readyStatus;
    private List<UUID> connectedPlayers;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public GameMaster() {
        teamBlueGuids = new ArrayList<UUID>();
        teamRedGuids = new ArrayList<UUID>();
        connectedPlayers = new ArrayList<UUID>();
        readyStatus = new HashMap<UUID, Boolean>();
        LOGGER.info("Game master created");
    }

    public GameMaster(String host, int portNumber, Path path) throws IOException, ParseException, UnexpectedActionException {
        LOGGER.info("Game master created");
        teamBlueGuids = new ArrayList<UUID>();
        teamRedGuids = new ArrayList<UUID>();
        connectedPlayers = new ArrayList<UUID>();
        readyStatus = new HashMap<UUID, Boolean>();
        this.loadConfigurationFromJson(path.toString());
        LOGGER.info("configuration loaded");
        simpleClient = new SimpleClient();
        simpleClient.startConnection(host, portNumber);
        LOGGER.info("connection started", host, portNumber);
        listen();
    }

    public void startGame() {
        if(readyStatus.size()>=teamRedGuids.size()+teamBlueGuids.size()){
            this.placePlayers();
        }
        LOGGER.info("Game started");
    }

    public void setupGame() throws GameSetupException {
        if(!this.configuration.checkData() || this.configuration==null) {
            throw new GameSetupException("empty configuration");
        }
        this.board = new GameMasterBoard(this.configuration.boardWidth, this.configuration.boardGoalHeight, this.configuration.boardTaskHeight);
        //this.status = GameMasterStatus.ACTIVE;
        for (Point pt : this.configuration.predefinedGoalPositions) {
            this.board.setGoal(new Position(pt.x, pt.y));
        }
        Position pos = new Position();
        for (int i = 0; i < this.configuration.initialPieces; i++) {
            pos = this.board.generatePiece();
            if (pos == null) {
                break;
            }
        }
        LOGGER.info("Game setup completed");
    }

    private void listen() throws IOException, ParseException, UnexpectedActionException {
        LOGGER.info("Game master has started listening");
        try {
            LOGGER.info("Pre while1");
            while(board == null)
            {
                LOGGER.info("Pre recive1");
                String msg = simpleClient.receiveMessage();
                LOGGER.info("post recive " + msg);
                if (msg != null && !msg.isEmpty()) {
                    LOGGER.info("Message received", msg);
                    JSONObject jsonObject = messageHandler(msg);
                    simpleClient.sendMessage(jsonObject.toJSONString());
                    LOGGER.info("Message returned", jsonObject.toJSONString());
                }
            }
            LOGGER.info("Pre while2");
            while (!board.checkWinCondition(TeamColor.RED) && !board.checkWinCondition(TeamColor.BLUE)) {
                LOGGER.info("Pre recive");
                String msg = simpleClient.receiveMessage();
                LOGGER.info("post recive");
                if (msg != null && !msg.isEmpty()) {
                    LOGGER.info("Message received", msg);
                    JSONObject jsonObject = messageHandler(msg);
                    simpleClient.sendMessage(jsonObject.toJSONString());
                    LOGGER.info("Message returned", jsonObject.toJSONString());
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Exception occured when listening " + e.toString(), e.toString());
        }
        LOGGER.info("Game master has finished listening");
    }

    public GameMasterConfiguration loadConfigurationFromJson(String path) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(new FileReader(path));
        JSONArray jsonArray = (JSONArray)jsonObject.get("predefinedGoalPositions");
        List<Point> pGP = new ArrayList<Point>();
        Iterator iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONArray elem = (JSONArray)iterator.next();
            pGP.add(new Point(((Long)elem.get(0)).intValue(), ((Long)elem.get(1)).intValue()));
        }
        Point[] xd;
        xd = (Point[])pGP.toArray(new Point[0]);
        GameMasterConfiguration gmc = new GameMasterConfiguration(((Double)jsonObject.get("shamProbability")), ((Long)jsonObject.get("maxTeamSize")).intValue(),
                                                                  ((Long)jsonObject.get("maxPieces")).intValue(), ((Long)jsonObject.get("initialPieces")).intValue(),
                                                                  xd, ((Long)jsonObject.get("boardWidth")).intValue(), ((Long)jsonObject.get("boardTaskHeight")).intValue(),
                                                                  ((Long)jsonObject.get("boardGoalHeight")).intValue(), ((Long)jsonObject.get("delayDestroyPiece")).intValue(),
                                                                  ((Long)jsonObject.get("delayNextPieceplace")).intValue(), ((Long)jsonObject.get("delayMove")).intValue(),
                                                                  ((Long)jsonObject.get("delayDiscover")).intValue(), ((Long)jsonObject.get("delayTest")).intValue(),
                                                                  ((Long)jsonObject.get("delayPick")).intValue(), ((Long)jsonObject.get("delayPlace")).intValue());
        this.configuration = gmc; // delete later
        return gmc;
    }

    public void saveConfigurationToJson(String path) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("shamProbability", this.configuration.shamProbability);
        jsonObject.put("maxTeamSize", this.configuration.maxTeamSize);
        jsonObject.put("maxPieces", this.configuration.maxPieces);
        jsonObject.put("initialPieces", this.configuration.initialPieces);
        JSONArray array = new JSONArray();
        for (int i = 0; i < this.configuration.predefinedGoalPositions.length; i++) {
            JSONArray xd = new JSONArray();
            xd.add(this.configuration.predefinedGoalPositions[i].x);
            xd.add(this.configuration.predefinedGoalPositions[i].y);
            array.add(xd);
        }
        jsonObject.put("predefinedGoalPositions", array);
        jsonObject.put("boardWidth", this.configuration.boardWidth);
        jsonObject.put("boardTaskHeight", this.configuration.boardTaskHeight);
        jsonObject.put("boardGoalHeight", this.configuration.boardGoalHeight);
        jsonObject.put("delayDestroyPiece", this.configuration.delayDestroyPiece);
        jsonObject.put("delayNextPieceplace", this.configuration.delayNextPieceplace);
        jsonObject.put("delayMove", this.configuration.delayMove);
        jsonObject.put("delayDiscover", this.configuration.delayDiscover);
        jsonObject.put("delayTest", this.configuration.delayTest);
        jsonObject.put("delayPick", this.configuration.delayPick);
        jsonObject.put("delayPlace", this.configuration.delayPlace);

        FileWriter file = new FileWriter(path);
        file.write(jsonObject.toJSONString());
        file.close();
        LOGGER.info("GMConfiguration saved");
    }

    private void putNewPiece() {

    }

    private void printBoard() {

    }

    private void deleteConfiguration() {
        this.configuration = null;
    }

    private void setReadyStatus(UUID uuid) throws PlayerNotConnectedException {
        if(connectedPlayers.contains(uuid)) {
            readyStatus.put(uuid, Boolean.TRUE);
        } else {
            throw new PlayerNotConnectedException("player not connected");
        }
        LOGGER.info("Player ", uuid, " set to ready");
    }

    private void placePlayers() {
        int bluePlayersToPlace = teamBlueGuids.size();
        int redPlayersToPlace = teamRedGuids.size();
        UUID currentUuid = teamBlueGuids.get(teamBlueGuids.size()-bluePlayersToPlace);
        PlayerDTO tmp = new PlayerDTO(currentUuid, TeamRole.MEMBER, null);
        tmp.playerTeamColor=TeamColor.BLUE;
        Position placed = new Position();
        try {
            for(int i=0;i<this.configuration.boardWidth;i++) {
                if(bluePlayersToPlace<=0) {
                    break;
                }
                for(int j=0;j<this.configuration.boardTaskHeight+this.configuration.boardGoalHeight;j++) {
                    if(bluePlayersToPlace<=0) {
                        break;
                    }
                    tmp.playerPosition = new Position(i,j);
                    tmp.playerGuid = currentUuid;
                    placed = this.board.placePlayer(tmp);
                    if(placed==null) {
                        continue;
                    }
                    bluePlayersToPlace--;
                    currentUuid = teamBlueGuids.get(teamBlueGuids.size()-bluePlayersToPlace);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error while placing blue players", e);
        }
        try {
            currentUuid = teamRedGuids.get(teamRedGuids.size()-redPlayersToPlace);
            for(int i=this.configuration.boardWidth-1;i>=0;i--) {
                if(redPlayersToPlace<=0) {
                    break;
                }
                for(int j=this.board.boardHeight-1;j>=0;j--) {
                    if(redPlayersToPlace<=0) {
                        break;
                    }
                    tmp.playerPosition = new Position(i,j);
                    tmp.playerGuid = currentUuid;
                    placed = this.board.placePlayer(tmp);
                    if(placed==null) {
                        continue;
                    }
                    redPlayersToPlace--;
                    currentUuid = teamRedGuids.get(teamRedGuids.size()-redPlayersToPlace);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while placing red players", e);
        }
        LOGGER.info("Players have been placed on the board");
    }

    // return type not specified in specifiaction
    public JSONObject messageHandler(String message) throws ParseException, JsonProcessingException, UnexpectedActionException {
        JSONParser jsonParser = new JSONParser();
        JSONObject msg = (JSONObject)jsonParser.parse(message);
        String action = (String)msg.get("action");
        //System.out.println(action);
        if(action.equals("setup")) {
            try {
                this.setupGame();
                //this.startGame();
                msg.put("status", "OK");
            } catch (Exception e) {
                msg.put("status", "DENIED");
            }
            //System.out.println("XD");
            return msg;
        }
        UUID uuid = UUID.fromString((String)msg.get("playerGuid"));
        String status = new String();
        switch (action) {
            // setup msgs
            case "setup":
                try {
                    this.setupGame();
                    //this.startGame();
                    msg.put("status", "OK");
                } catch (Exception e) {
                    msg.put("status", "DENIED");
                }
                return msg;
            case "connect":
                if (teamRedGuids.size() >= this.configuration.maxTeamSize && teamRedGuids.size() >= this.configuration.maxTeamSize) {
                    msg.put("status", "DENIED");
                    return msg;
                }
                connectedPlayers.add(uuid);
                if (teamBlueGuids.size() < this.configuration.maxTeamSize) {
                    teamBlueGuids.add(uuid);
                } else if (teamRedGuids.size() < this.configuration.maxTeamSize) {
                    teamRedGuids.add(uuid);
                }
                msg.put("status", "OK");
                return msg;
            case "ready":
                try {
                    setReadyStatus(uuid);
                    msg.put("status", "YES");
                } catch (Exception e) {
                    msg.put("status", "NO");
                }
                return msg;

            // game action msgs
            case "move":
                String directionString = (String)msg.get("direction");
                Position.Direction direction;
                switch (directionString) {
                    case "Up":
                        direction = Position.Direction.UP;
                        break;
                    case "Down":
                        direction = Position.Direction.DOWN;
                        break;
                    case "Left":
                        direction = Position.Direction.LEFT;
                        break;
                    case "Right":
                        direction = Position.Direction.RIGHT;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + directionString); // implement new exception later on
                }
                PlayerDTO playerDTO = playersDTO.get(uuid);
                Position newPosition = board.playerMove(playerDTO, direction);
                JSONObject positionJSON = new JSONObject();
                if (newPosition.x == -1 && newPosition.y == -1) {
                    status = "DENIED";
                    msg.put("position", null);
                } else {
                    status = "OK";
                    positionJSON.put("x", newPosition.x);
                    positionJSON.put("y", newPosition.y);
                    msg.put("position", positionJSON);
                }
                msg.put("status", status);
                return msg;
            // implement sending msg back to player
            case "pickup":
                Position pos = playersDTO.get(uuid).playerPosition;
                Cell.CellState res = board.takePiece(pos);
                if (res == Cell.CellState.VALID || res == Cell.CellState.PIECE) {
                    status = "OK";
                } else {
                    status = "DENIED";
                }
                msg.put("status", status);
                // implement sending msg back to player
                return msg;
            case "test":
                Field xd = this.board.getField(playersDTO.get(uuid).playerPosition);
                Cell.CellState state = xd.cell.cellState;
                if (state != Cell.CellState.PIECE) {
                    msg.put("status", "DENIED");
                    msg.put("test", null);
                } else {
                    boolean boolStatus = Math.random() > (1 - configuration.shamProbability);
                    msg.put("test", boolStatus);
                    msg.put("status", "OK");
                }
                // implement sending msg back
                return msg;
            case "place":
                Field xdd = this.board.getField(playersDTO.get(uuid).playerPosition);
                Cell.CellState state2 = xdd.cell.cellState;
                if (state2 == Cell.CellState.PIECE) {
                    msg.put("status", "DENIED");
                    msg.put("placementResult", null);
                    break;
                }
                PlacementResult res2 = board.placePiece(playersDTO.get(uuid));
                if (res2 == PlacementResult.CORRECT) {
                    msg.put("placementResult", "Correct");
                } else if (res2 == PlacementResult.POINTLESS) {
                    msg.put("placementResult", "Pointless");
                }
                // implement sending msg back
                return msg;
            case "discover":
                List<Field> fieldList = board.discover(playersDTO.get(uuid).playerPosition);
                ObjectMapper mapper = new ObjectMapper();
                msg.put("fields", mapper.writeValueAsString(fieldList));
                // implement sending msg back
                return msg;

            default:
                throw new UnexpectedActionException("Unexpected value: " + action);
        }
        throw new UnexpectedActionException("Unexpected behaviour");
    }


}
