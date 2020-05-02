package com.pw.server.network;

import com.pw.server.model.PlayerMessage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerReader extends Thread {
    private final static Logger LOGGER = LoggerFactory.getLogger(PlayerReader.class);
    private AtomicBoolean running = new AtomicBoolean(true);

    private Socket socket;
    private IOHandler ioHandler;
    private BlockingQueue<PlayerMessage> messages;
    private int playerNumber;

    public PlayerReader(Socket socket, IOHandler ioHandler, BlockingQueue<PlayerMessage> messages, int playerNumber) {
        this.socket = socket;
        this.ioHandler = ioHandler;
        this.messages = messages;
        this.playerNumber = playerNumber;
    }

    @SneakyThrows
    @Override
    public void run() {
        LOGGER.info("Reading messages from player {} started", playerNumber);

        BufferedReader reader = ioHandler.getReader(socket.getInputStream());

        while (running.get()) {
            String line;
            if (reader.ready() && (line = reader.readLine()) != null) {
                messages.put(new PlayerMessage(playerNumber, line));
            }
        }

        LOGGER.info("Reading messages from player {} ended", playerNumber);
    }

    public void stopRunning() {
        running.set(false);
    }
}
