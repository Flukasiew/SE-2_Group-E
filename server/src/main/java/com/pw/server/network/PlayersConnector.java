package com.pw.server.network;

import com.pw.server.model.Config;
import com.pw.server.model.PlayerMessage;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;

@Data
public class PlayersConnector extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayersConnector.class);

    private AtomicBoolean running = new AtomicBoolean(true);

    private ServerSocket serverSocket;
    private IOHandler ioHandler;
    private BlockingQueue<PlayerMessage> messages;
    private Map<String, PrintWriter> playerWriters;
    private Config config;

    private List<PlayerReader> playerReaders = newArrayList();

    public PlayersConnector(ServerSocket serverSocket, IOHandler ioHandler, BlockingQueue<PlayerMessage> messages,
                            Map<String, PrintWriter> playerWriters, Config config) {
        this.serverSocket = serverSocket;
        this.ioHandler = ioHandler;
        this.messages = messages;
        this.playerWriters = playerWriters;
        this.config = config;
    }

    @Override
    public void run() {
        int connections = 0;

        try {
            serverSocket.setSoTimeout(config.getPlayerConnectionTimeout());
        } catch (SocketException e) {
            LOGGER.error(e.getLocalizedMessage());
        }

        LOGGER.info("Accepting player connections...");
        while (connections <= config.getPlayerConnectionsLimit() && running.get()) {
            Socket playerSocket;
            try {
                playerSocket = serverSocket.accept();
            } catch (Exception e) {
                LOGGER.info("Accepting player connection interrupted", e);
                break;
            }

            connections += 1;
            LOGGER.info("Connection with a new player established");

            new PlayerReader(playerSocket, ioHandler, messages, playerWriters).start();
        }

        LOGGER.info("Accepting player connections ended");
    }

    public void stopRunning() {
        running.set(false);
        try {
            serverSocket.close();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }

        playerReaders.forEach(PlayerReader::stopRunning);
    }
}
