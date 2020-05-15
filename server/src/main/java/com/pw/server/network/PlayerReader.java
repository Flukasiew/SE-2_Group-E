package com.pw.server.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.PlayerConnectMessageDTO;
import com.pw.server.exception.PlayerSetupException;
import com.pw.server.model.PlayerMessage;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class PlayerReader extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerReader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private AtomicBoolean running = new AtomicBoolean(true);

    private Socket socket;
    private IOHandler ioHandler;
    private BlockingQueue<PlayerMessage> messages;
    private Map<String, PrintWriter> playerWriters;
    private String playerGuid;
    private BufferedReader reader;

    public PlayerReader(Socket socket, IOHandler ioHandler, BlockingQueue<PlayerMessage> messages, Map<String,
            PrintWriter> playerWriters) {
        this.socket = socket;
        this.ioHandler = ioHandler;
        this.messages = messages;
        this.playerWriters = playerWriters;
    }

    @SneakyThrows
    @Override
    public void run() {
        reader = ioHandler.getReader(socket.getInputStream());

        setup();
        readMessages();
    }

    private void setup() throws PlayerSetupException, IOException, InterruptedException {
        String message = reader.readLine();
        PlayerConnectMessageDTO playerConnectMessageDTO;
        try {
            playerConnectMessageDTO = MAPPER.readValue(message, PlayerConnectMessageDTO.class);
        } catch (IOException e) {
            throw new PlayerSetupException("Invalid message format: not a connect message", e);
        }

        if (playerConnectMessageDTO.getPlayerGuid() == null) {
            throw new PlayerSetupException("No player guid provided");
        }

        playerGuid = playerConnectMessageDTO.getPlayerGuid();

        playerWriters.put(playerGuid, ioHandler.getWriter(socket.getOutputStream()));

        messages.put(new PlayerMessage(playerGuid, message));

        LOGGER.info("Setup for player {} completed", playerGuid);
    }

    private void readMessages() throws Exception {
        LOGGER.info("Reading messages from player {}...", playerGuid);

        while (running.get()) {
            String line;
            if (reader.ready() && (line = reader.readLine()) != null) {
                messages.put(new PlayerMessage(playerGuid, line));
            }
        }

        LOGGER.info("Reading messages from player {} ended", playerGuid);
    }

    public void stopRunning() {
        running.set(false);

        try {
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.error("Unable to close player writer", e);
        }
    }
}
