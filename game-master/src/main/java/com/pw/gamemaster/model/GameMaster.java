package com.pw.gamemaster.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.common.dto.PlayerDTO;
import com.pw.common.model.*;
import com.pw.common.model.Action;
import com.pw.common.model.SimpleClient;
import com.pw.gamemaster.exception.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static java.lang.Thread.sleep;


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
    private Map<UUID, PlayerDTO> playersDTO;
    private Map<UUID, Boolean> readyStatus;
    private Map<UUID, Boolean> playerPieces;
    private List<UUID> connectedPlayers;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private long timer;

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
        playersDTO = new HashMap<UUID, PlayerDTO>();
        this.loadConfigurationFromJson(path.toString());
        LOGGER.info("configuration loaded");
        simpleClient = new SimpleClient();
        simpleClient.startConnection(host, portNumber);
        LOGGER.info("connection started", host, portNumber);
    }

    private void initPlayerPieces() {
        playerPieces = new HashMap<UUID, Boolean>();
        for (UUID uuid : connectedPlayers) {
            playerPieces.put(uuid, false);
        }
    }

    public void startGame() throws IOException {
        this.placePlayers();
        PlayerDTO playerDTO;
        JSONArray jsonArrayBlueGuids = new JSONArray();
        JSONArray jsonArrayRedGuids = new JSONArray();
        for (UUID uuid : teamBlueGuids) {
            jsonArrayBlueGuids.add(uuid.toString());
        }
        for (UUID uuid : teamRedGuids) {
            jsonArrayRedGuids.add(uuid.toString());
        }
        for (UUID player : connectedPlayers) {
            JSONObject jsonObject = new JSONObject();
            JSONObject positionJsonObject = new JSONObject();
            JSONObject boardJsonObject = new JSONObject();
            playerDTO = playersDTO.get(player);
            jsonObject.put("action", "start");
            jsonObject.put("playerGuid", player.toString());
            jsonObject.put("team", playerDTO.playerTeamColor.toString());
            jsonObject.put("teamRole", playerDTO.playerTeamRole.toString());
            jsonObject.put("teamSize", teamBlueGuids.size());
            if (playerDTO.getPlayerTeamColor() == TeamColor.Blue) {
                jsonObject.put("teamGuids", jsonArrayBlueGuids);
            } else {
                jsonObject.put("teamGuids", jsonArrayRedGuids);
            }
            positionJsonObject.put("x", playerDTO.playerPosition.x);
            positionJsonObject.put("y", playerDTO.playerPosition.y);
            jsonObject.put("position", positionJsonObject);
            boardJsonObject.put("boardWidth", this.board.boardWidth);
            boardJsonObject.put("taskAreaHeight", this.board.taskAreaHeight);
            boardJsonObject.put("goalAreaHeight", this.board.goalAreaHeight);
            jsonObject.put("board", boardJsonObject);
            simpleClient.sendMessage(jsonObject.toJSONString());
        }
        initPlayerPieces();

        LOGGER.info("Game started");
    }

    public void setupGame() throws GameSetupException {
        if (!this.configuration.checkData() || this.configuration == null) {
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

    public boolean checkReadyGame() {
        //            for (UUID id: connectedPlayers) {
        //                JSONObject jsonObject = new JSONObject();
        //                jsonObject.put("action", "ready");
        //                jsonObject.put("playerGuid", id.toString());
        //                try {
        //                    simpleClient.sendMessage(jsonObject.toJSONString());
        //                } catch (IOException e) {
        //                    LOGGER.error("Error Ready" + e.toString(), e);
        //                }
        //            }
        return (teamRedGuids.size() + teamBlueGuids.size()) == 2 * this.configuration.maxTeamSize &&
                2 * this.configuration.maxTeamSize == connectedPlayers.size();
    }

    public void listen() throws IOException, ParseException, UnexpectedActionException {
        String msg = "empty";
        Boolean redWin = false, blueWin = false, timeE = true;
        GameMessageEndDTO gameMessageEndDTO = new GameMessageEndDTO(Action.end, null);
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info("Game master has started listening");
        try {
            sleep(1000);
            // wait for setup
//            LOGGER.info("Waiting for setup");
//            timer = System.currentTimeMillis();
//            while (board == null && (timeE = System.currentTimeMillis() - timer < this.configuration.startupTimeLimit)) {
//                msg = simpleClient.receiveMessage();
//                if (msg != null && !msg.isEmpty()) {
//                    LOGGER.info("Message received", msg);
//                    JSONObject jsonObject = messageHandler(msg);
//                    simpleClient.sendMessage(jsonObject.toJSONString());
//                    LOGGER.info("Message returned", jsonObject.toJSONString());
//                    //startTime = System.currentTimeMillis();
//                }
//            }
//            LOGGER.info("Setup loop done");
//            if (!timeE) {
//                throw new TimeLimitExceededException("Game message time limit exceeded");
//            } else if (board == null) {
//                throw new SetupFailedException("Board is null");
//            }
            this.setupGame();
            // wait for players
            LOGGER.info("Waiting for players");
            timer = System.currentTimeMillis();
            while (!checkReadyGame() && (timeE = System.currentTimeMillis() - timer < this.configuration.playersConnectTimeLimit)) {
                msg = simpleClient.receiveMessage();
                if (msg != null && !msg.isEmpty()) {
                    LOGGER.info("Message received", msg);
                    JSONObject jsonObject = messageHandler(msg);
                    simpleClient.sendMessage(jsonObject.toJSONString());
                    LOGGER.info("Message returned", jsonObject.toJSONString());
                }
            }
            LOGGER.info("Player connections loop done");
            if (!timeE) {
                throw new TimeLimitExceededException("Game message time limit exceeded");
            } else if (!checkReadyGame()) {
                throw new GameNotReadyException("Game tried to start when it was not ready");
            }
            // play actual game
            startGame();
            timer = System.currentTimeMillis();
            while (!(redWin = board.checkWinCondition(TeamColor.Red)) && !(blueWin = board.checkWinCondition(TeamColor.Blue)) && (timeE = System.currentTimeMillis() - timer < this.configuration.gameMsgTimeLimit)) {
                msg = simpleClient.receiveMessage();
                if (msg != null && !msg.isEmpty()) {
                    LOGGER.info("Message received", msg);
                    JSONObject jsonObject = messageHandler(msg);
                    simpleClient.sendMessage(jsonObject.toJSONString());
                    LOGGER.info("Message returned", jsonObject.toJSONString());
                    timer = System.currentTimeMillis();
                }
            }
            if (!timeE) {
                throw new TimeLimitExceededException("Game message time limit exceeded");
            }
            LOGGER.info("Play game loop done");
            if (blueWin) {
                gameMessageEndDTO = new GameMessageEndDTO(Action.end, GameEndResult.Blue);
                LOGGER.info("Blue team has won");
            } else if (redWin) {
                gameMessageEndDTO = new GameMessageEndDTO(Action.end, GameEndResult.Red);
                LOGGER.info("Red team has won");
            } else {
                LOGGER.warn("No winner has been decided");
                gameMessageEndDTO = new GameMessageEndDTO(Action.end, null);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured when listening " + e.toString());
        } finally {
            simpleClient.sendMessage(objectMapper.writeValueAsString(gameMessageEndDTO));
            LOGGER.info("End game message sent");
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
        xd = pGP.toArray(new Point[0]);
        GameMasterConfiguration gmc = new GameMasterConfiguration(((Double)jsonObject.get("shamProbability")), ((Long)jsonObject.get("maxTeamSize")).intValue(),
                                                                  ((Long)jsonObject.get("maxPieces")).intValue(), ((Long)jsonObject.get("initialPieces")).intValue(),
                                                                  xd, ((Long)jsonObject.get("boardWidth")).intValue(), ((Long)jsonObject.get("boardTaskHeight")).intValue(),
                                                                  ((Long)jsonObject.get("boardGoalHeight")).intValue(), ((Long)jsonObject.get("delayDestroyPiece")).intValue(),
                                                                  ((Long)jsonObject.get("delayNextPieceplace")).intValue(), ((Long)jsonObject.get("delayMove")).intValue(),
                                                                  ((Long)jsonObject.get("delayDiscover")).intValue(), ((Long)jsonObject.get("delayTest")).intValue(),
                                                                  ((Long)jsonObject.get("delayPick")).intValue(), ((Long)jsonObject.get("delayPlace")).intValue(),
                                                                  (Long)jsonObject.get("startupTimeLimitInSeconds"), (Long)jsonObject.get("playersConnectTimeLimitInSeconds"),
                                                                  (Long)jsonObject.get("gameMsgTimeLimitInSeconds"));
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
        jsonObject.put("gameMsgTimeLimitInSeconds", this.configuration.gameMsgTimeLimit / 1000);
        jsonObject.put("playersConnectTimeLimitInSeconds", this.configuration.playersConnectTimeLimit / 1000);
        jsonObject.put("startupTimeLimitInSeconds", this.configuration.startupTimeLimit / 1000);


        FileWriter file = new FileWriter(path);
        file.write(jsonObject.toJSONString());
        file.close();
        LOGGER.info("GMConfiguration saved");
    }

    private void putNewPiece() {
        if (this.board != null) {
            LOGGER.info("Placing new piece on the board");
            this.board.generatePiece();
        }
    }

    private void deleteConfiguration() {
        this.configuration = null;
    }

    private void setReadyStatus(UUID uuid) throws PlayerNotConnectedException {
        if (connectedPlayers.contains(uuid)) {
            readyStatus.put(uuid, Boolean.TRUE);
        } else {
            throw new PlayerNotConnectedException("player not connected");
        }
        LOGGER.info("Player ", uuid, " set to ready");
    }

    private void placePlayers() {
        int bluePlayersToPlace = teamBlueGuids.size();
        int redPlayersToPlace = teamRedGuids.size();
        UUID currentUuid = teamBlueGuids.get(teamBlueGuids.size() - bluePlayersToPlace);
        Position placed = new Position();
        try {
            for (int i = 0; i < this.configuration.boardWidth; i++) {
                if (bluePlayersToPlace <= 0) {
                    break;
                }
                for (int j = 0; j < this.configuration.boardTaskHeight + this.configuration.boardGoalHeight; j++) {
                    PlayerDTO tmp = new PlayerDTO(currentUuid, TeamRole.MEMBER, null);
                    tmp.playerTeamColor = TeamColor.Blue;
                    if (bluePlayersToPlace <= 0) {
                        break;
                    }
                    tmp.playerPosition = new Position(i, j);
                    tmp.playerGuid = currentUuid;
                    placed = this.board.placePlayer(tmp);
                    if (placed == null) {
                        continue;
                    }
                    bluePlayersToPlace--;
                    playersDTO.put(tmp.playerGuid, tmp);
                    if (bluePlayersToPlace <= 0) {
                        break;
                    }
                    currentUuid = teamBlueGuids.get(teamBlueGuids.size() - bluePlayersToPlace);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while placing blue players", e);
        }
        try {
            currentUuid = teamRedGuids.get(teamRedGuids.size() - redPlayersToPlace);
            for (int i = this.configuration.boardWidth - 1; i >= 0; i--) {
                if (redPlayersToPlace <= 0) {
                    break;
                }
                for (int j = this.board.boardHeight - 1; j >= 0; j--) {
                    PlayerDTO tmp = new PlayerDTO(currentUuid, TeamRole.MEMBER, null);
                    tmp.playerTeamColor = TeamColor.Red;
                    if (redPlayersToPlace <= 0) {
                        break;
                    }
                    tmp.playerPosition = new Position(i, j);
                    tmp.playerGuid = currentUuid;
                    placed = this.board.placePlayer(tmp);
                    if (placed == null) {
                        continue;
                    }
                    redPlayersToPlace--;
                    playersDTO.put(tmp.playerGuid, tmp);
                    if (redPlayersToPlace <= 0) {
                        break;
                    }
                    currentUuid = teamRedGuids.get(teamRedGuids.size() - redPlayersToPlace);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while placing red players", e);
        }
        LOGGER.info("Players have been placed on the board");
    }

    // return type not specified in specifiaction
    public JSONObject messageHandler(String message) throws ParseException, UnexpectedActionException, IOException, InvalidMoveException {
        JSONParser jsonParser = new JSONParser();
        JSONObject msg = (JSONObject)jsonParser.parse(message);
        String action = (String)msg.get("action");
        //System.out.println(action);
        if (action.equals("setup")) {
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
        String status = "";
        switch (action) {
            // setup msgs
            case "setup":
                try {
                    LOGGER.info("Setup message received");
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
                LOGGER.info("Player " + uuid.toString() + " connected");
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
                        direction = Position.Direction.Up;
                        break;
                    case "Down":
                        direction = Position.Direction.Down;
                        break;
                    case "Left":
                        direction = Position.Direction.Left;
                        break;
                    case "Right":
                        direction = Position.Direction.Right;
                        break;
                    default:
                        throw new InvalidMoveException("Unexpected value: " + directionString); // implement new exception later on
                }
                PlayerDTO playerDTO = playersDTO.get(uuid);
                Position newPosition = board.playerMove(playerDTO, direction);
                JSONObject positionJSON = new JSONObject();
                if (newPosition == null) {
                    status = "DENIED";
                    msg.put("position", null);
                    LOGGER.info("Move from player " + uuid.toString() + " denied");
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
                if (res == Cell.CellState.Valid || res == Cell.CellState.Piece) {
                    status = "OK";
                    playerPieces.replace(uuid, true);
                    this.putNewPiece();
                } else {
                    status = "DENIED";
                }
                msg.put("status", status);
                // implement sending msg back to player
                return msg;
            case "test":
                //Field xd = this.board.getField(playersDTO.get(uuid).playerPosition);
                //Cell.CellState state = xd.cell.cellState;
                if (!playerPieces.get(uuid)) {
                    msg.put("status", "DENIED");
                    msg.put("test", null);
                } else {
                    boolean boolStatus = Math.random() > (1 - configuration.shamProbability);
                    if (!boolStatus) {
                        playerPieces.replace(uuid, false);
                        this.putNewPiece();
                    }
                    msg.put("test", boolStatus);
                    msg.put("status", "OK");
                }
                return msg;
            case "place":
                Field xdd = this.board.getField(playersDTO.get(uuid).playerPosition);
                Cell.CellState state2 = xdd.cell.cellState;
                if (state2 == Cell.CellState.Piece || !playerPieces.get(uuid)) {
                    msg.put("status", "DENIED");
                    msg.put("placementResult", null);
                } else {
                    PlacementResult res2 = board.placePiece(playersDTO.get(uuid));
                    if (res2 == PlacementResult.CORRECT) {
                        msg.put("status", "OK");
                        msg.put("placementResult", "Correct");
                        playerPieces.replace(uuid, false);
                    } else if (res2 == PlacementResult.POINTLESS) {
                        msg.put("status", "OK");
                        msg.put("placementResult", "Pointless");
                        playerPieces.replace(uuid, false);
                    }
                }
                return msg;
            case "discover":
                List<Field> fieldList = board.discover(playersDTO.get(uuid).playerPosition);
                //ObjectMapper mapper = new ObjectMapper();
                JSONArray jsonArray = new JSONArray();
                if (fieldList.size() == 0) {
                    msg.put("status", "DENIED");
                } else {
                    msg.put("status", "OK");
                }
                for (Field field :fieldList)
                {
                    JSONObject positionJSONObject = new JSONObject();
                    JSONObject fieldJSONObject = new JSONObject();
                    positionJSONObject.put("x", field.position.x);
                    positionJSONObject.put("y", field.position.y);
                    fieldJSONObject.put("position",positionJSONObject);
                    fieldJSONObject.put("cell",field.cell.getJson());
                    jsonArray.add(fieldJSONObject);
                }

                msg.put("fields", jsonArray);

                return msg;

            default:
                throw new UnexpectedActionException("Unexpected value: " + action);
        }
        //throw new UnexpectedActionException("Unexpected behaviour");
    }


}
