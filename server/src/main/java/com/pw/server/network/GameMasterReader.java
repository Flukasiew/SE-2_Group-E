package com.pw.server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class GameMasterReader extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMasterReader.class);
    private AtomicBoolean running = new AtomicBoolean(true);

    private Socket socket;
    private IOHandler ioHandler;
    private BlockingQueue<String> messages;

    public GameMasterReader(Socket socket, IOHandler ioHandler, BlockingQueue<String> messages) {
        this.socket = socket;
        this.ioHandler = ioHandler;
        this.messages = messages;
    }

    @SneakyThrows
    @Override
    public void run() {
        BufferedReader reader = ioHandler.getReader(socket.getInputStream());

        LOGGER.info("Reading messages from game master...");
        while (running.get()) {
            String line;
            if (reader.ready() && (line = reader.readLine()) != null) {
                messages.put(line);
            }
        }

        reader.close();
        LOGGER.info("Reading messages from game master ended");
    }

    public void stopRunning() {
        running.set(false);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.error("Unable to close game master reader", e);
        }
        LOGGER.info("Game master reader stopped");
    }
}
