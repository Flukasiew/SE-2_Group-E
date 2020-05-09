package com.pw.server.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.server.exception.UnrecognizedMessageException;
import com.pw.server.factory.Factory;
import com.pw.server.model.Config;
import com.pw.server.model.PlayerMessage;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class CommunicationServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationServer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PLAYER_GUID = "playerGuid";

    private int portNumber;
    private String ipAddress;
    private Config config;

    private Factory factory = new Factory();

    private BlockingQueue<PlayerMessage> messagesFromPlayers = new LinkedBlockingQueue<>();
    private BlockingQueue<String> messagesFromGameMaster = new LinkedBlockingQueue<>();

    private PlayersConnector playersConnector;
    private GameMasterConnector gameMasterConnector;

    private IOHandler ioHandler = new IOHandler();
    private PrintWriter gameMasterWriter;
    private Map<String, PrintWriter> playerWriters = new ConcurrentHashMap<>();

    private Boolean endGame = false;

    public CommunicationServer(Config config) {
        this.portNumber = config.getPortNumber();
        this.ipAddress = config.getIpAddress();
        this.config = config;
    }

    public void listen() throws Exception {
        LOGGER.info("Listening started");
        ServerSocket serverSocket = factory.createServerSocket(portNumber);

        setupGameMaster(serverSocket);
        setupPlayers(serverSocket);

        relayMessages();

        LOGGER.info("Listening ended");
    }

    private void setupGameMaster(ServerSocket serverSocket) throws Exception {
        gameMasterConnector = factory.createGameMasterConnector(serverSocket, ioHandler, messagesFromGameMaster,
                config);
        gameMasterConnector.connect();
        gameMasterConnector.setup();
        gameMasterWriter = gameMasterConnector.getWriter();

        LOGGER.info("Game master setup completed");
    }

    private void setupPlayers(ServerSocket serverSocket) {
        playersConnector = factory.createPlayersConnector(serverSocket, ioHandler, messagesFromPlayers, playerWriters
                , config);
        playersConnector.start();

        LOGGER.info("Players setup initiated");
    }

    public void relayMessages() throws InterruptedException, UnrecognizedMessageException {
        LOGGER.info("Relaying messages started");

        while (!endGame) {
            if (!messagesFromPlayers.isEmpty()) {
                handleMessageFromPlayer(messagesFromPlayers.take());
            }
            if (!messagesFromGameMaster.isEmpty()) {
                handleMessageFromGameMaster(messagesFromGameMaster.take());
            }
        }

        gameMasterConnector.stopRunning();
        playersConnector.stopRunning();

        LOGGER.info("Relaying messages ended");
    }

    private void handleMessageFromPlayer(PlayerMessage message) {
        LOGGER.info("Sending message to game master");

        gameMasterWriter.println(message.getMessage());
    }

    private void handleMessageFromGameMaster(String message) throws UnrecognizedMessageException {
        GameMessageEndDTO gameMessageEndDTO = getEndGameMessage(message);
        if (gameMessageEndDTO != null) {
            endGame(gameMessageEndDTO);
            return;
        }

        Map<String, Object> messageJson;
        try {
            messageJson = MAPPER.readValue(message, Map.class);
        } catch (Exception e) {
            throw new UnrecognizedMessageException("Cannot parse JSON", e);
        }

        if (!messageJson.containsKey(PLAYER_GUID) || !(messageJson.get(PLAYER_GUID) instanceof String)) {
            throw new UnrecognizedMessageException("Message does not contain player guid");
        }

        String playerGuid = (String) messageJson.get(PLAYER_GUID);
        LOGGER.info("Sending message to player {}", playerGuid);

        PrintWriter playerWriter = playerWriters.get(playerGuid);
        if (playerWriter != null) {
            playerWriter.println(message);
        } else {
            LOGGER.error("No writer for {} player", playerGuid);
        }
    }

    private GameMessageEndDTO getEndGameMessage(String message) {
        GameMessageEndDTO gameMessageEndDTO;
        try {
            gameMessageEndDTO = MAPPER.readValue(message, GameMessageEndDTO.class);
        } catch (Exception e) {
            return null;
        }
        return gameMessageEndDTO;
    }

    private void endGame(GameMessageEndDTO gameMessageEndDTO) {
        endGame = true;
    }
}
