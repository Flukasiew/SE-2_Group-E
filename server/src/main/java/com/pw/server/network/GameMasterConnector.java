package com.pw.server.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameSetupDTO;
import com.pw.common.dto.GameSetupStatusDTO;
import com.pw.common.model.Action;
import com.pw.common.model.GameSetupStatus;
import com.pw.server.exception.GameMasterSetupException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

@Data
public class GameMasterConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMasterConnector.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int GAME_MASTER_TIMEOUT = 10 * 1000;

    private ServerSocket serverSocket;
    private IOHandler ioHandler;
    private Socket gameMasterSocket;
    private BlockingQueue<String> messages;

    private PrintWriter writer;
    private BufferedReader reader;

    public GameMasterConnector(ServerSocket serverSocket, IOHandler ioHandler, BlockingQueue<String> messages) {
        this.serverSocket = serverSocket;
        this.ioHandler = ioHandler;
        this.messages = messages;

        try {
            serverSocket.setSoTimeout(GAME_MASTER_TIMEOUT);
        } catch (SocketException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    public void connect() throws Exception {
        LOGGER.info("Setting up game master started");

        try {
            gameMasterSocket = serverSocket.accept();
        } catch (IOException e) {
            throw new GameMasterSetupException("Accepting game master connection interrupted", e);
        }
        LOGGER.info("Connection with game master established");

        writer = ioHandler.getWriter(gameMasterSocket.getOutputStream());
        reader = ioHandler.getReader(gameMasterSocket.getInputStream());
    }

    public void setup() throws IOException, GameMasterSetupException {
        GameSetupDTO gameSetupDTO = new GameSetupDTO(Action.setup);
        writer.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(gameSetupDTO));
        LOGGER.info("Setup message sent to game master");

        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            GameSetupStatusDTO gameSetupStatusDTO = MAPPER.readValue(inputLine, GameSetupStatusDTO.class);
            if (gameSetupStatusDTO != null && gameSetupStatusDTO.getAction() == Action.setup) {
                if (gameSetupStatusDTO.getStatus() == GameSetupStatus.OK) {
                    LOGGER.info("Game master returned OK status");
                    new GameMasterReader(gameMasterSocket, ioHandler, messages).start();
                    break;
                } else if (gameSetupStatusDTO.getStatus() == GameSetupStatus.DENIED) {
                    throw new GameMasterSetupException("Game master returned DENIED setup status");
                }
            }
        }
    }
}
