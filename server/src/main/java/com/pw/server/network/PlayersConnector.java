package com.pw.server.network;

import com.pw.server.model.PlayerMessage;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class PlayersConnector extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayersConnector.class);
    private static final int MAX_PLAYER_COUNT = 16;

    private AtomicBoolean running = new AtomicBoolean(true);

    private ServerSocket serverSocket;
    private IOHandler ioHandler;
    private BlockingQueue<PlayerMessage> messages;
    private ConcurrentMap<Integer, PrintWriter> playerWriters;

    public PlayersConnector(ServerSocket serverSocket, IOHandler ioHandler, BlockingQueue<PlayerMessage> messages,
                            ConcurrentMap<Integer, PrintWriter> playerWriters) {
        this.serverSocket = serverSocket;
        this.ioHandler = ioHandler;
        this.messages = messages;
        this.playerWriters = playerWriters;
    }

    @Override
    public void run() {
        int playerCount = 0;

        LOGGER.info("Accepting player connections started");
        while (playerCount <= MAX_PLAYER_COUNT && running.get()) {
            Socket playerSocket;
            try {
                playerSocket = serverSocket.accept();
            } catch (Exception e) {
                LOGGER.error("Accepting player connection interrupted", e);
                break;
            }

            playerCount += 1;
            LOGGER.info("Connection with player {} established", playerCount);

            try {
                playerWriters.put(playerCount, ioHandler.getWriter(playerSocket.getOutputStream()));
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }

            new PlayerReader(playerSocket, ioHandler, messages, playerCount).start();
        }

        LOGGER.info("Accepting player connections stopped");
    }

    public void stopRunning() {
        running.set(false);
        try {
            serverSocket.close();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }
}
