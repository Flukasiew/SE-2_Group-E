package com.pw.server.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.server.factory.Factory;
import com.pw.server.model.PlayerMessage;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class CommunicationServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationServer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private int portNumber;
    private String ipAddress;

    private Factory factory = new Factory();

    private BlockingQueue<PlayerMessage> messagesFromPlayers = new LinkedBlockingQueue<>();
    private BlockingQueue<String> messagesFromGameMaster = new LinkedBlockingQueue<>();

    private IOHandler ioHandler = new IOHandler();
    private PrintWriter gameMasterWriter;
    private ConcurrentMap<Integer, PrintWriter> playerWriters = new ConcurrentHashMap<>();

    private Queue<Integer> responseQueue = new LinkedList<>();
    private Boolean endGame = false;

    public CommunicationServer(int portNumber, String ipAddress) {
        this.portNumber = portNumber;
        this.ipAddress = ipAddress;
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
        GameMasterConnector gameMasterConnector = factory.createGameMasterConnector(serverSocket, ioHandler, messagesFromGameMaster);
        gameMasterConnector.connect();
        gameMasterConnector.setup();
        gameMasterWriter = gameMasterConnector.getWriter();

        LOGGER.info("Game master setup completed");
    }

    private void setupPlayers(ServerSocket serverSocket) {
        PlayersConnector playersConnector = factory.createPlayersConnector(serverSocket, ioHandler, messagesFromPlayers, playerWriters);
        playersConnector.start();

        LOGGER.info("Players setup initiated");
    }

    public void relayMessages() throws InterruptedException {
        LOGGER.info("Relaying messages started");

        while (!endGame) {
            if (!messagesFromPlayers.isEmpty()) {
                handleMessageFromPlayer(messagesFromPlayers.take());
            }
            if (!messagesFromGameMaster.isEmpty()) {
                handleMessageFromGameMaster(messagesFromGameMaster.take());
            }
        }

        LOGGER.info("Relay messages ended");
    }

    private void handleMessageFromPlayer(PlayerMessage message) {
        LOGGER.info("Sending message to game master");

        responseQueue.add(message.getPlayerNumber());
        gameMasterWriter.println(message.getMessage());
    }

    private void handleMessageFromGameMaster(String message) {
        boolean endMessage = true;
        GameMessageEndDTO gameMessageEndDTO = null;
        try {
            gameMessageEndDTO = MAPPER.readValue(message, GameMessageEndDTO.class);
        } catch (Exception e) {
            endMessage = false;
        }
        if (endMessage && gameMessageEndDTO != null) {
            endGame = true;
            return;
        }

        if (responseQueue.isEmpty()) {
            LOGGER.error("No one to send the message to: {}", message);
            return;
        }

        int playerNumber = responseQueue.poll();
        LOGGER.info("Sending message to player {}", playerNumber);

        PrintWriter playerWriter = playerWriters.get(playerNumber);
        if (playerWriter != null) {
            playerWriter.println(message);
        }
    }
}
