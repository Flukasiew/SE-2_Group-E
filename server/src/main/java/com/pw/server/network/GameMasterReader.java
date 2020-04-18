package com.pw.server.network;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
        LOGGER.info("Reading messages from game master started");

        BufferedReader reader = ioHandler.getReader(socket.getInputStream());

        while (running.get()) {
            String line;
            if (reader.ready() && (line = reader.readLine()) != null) {
                messages.put(line);
            }
        }

        LOGGER.info("Reading messages from game master ended");
    }

    public void stopRunning() {
        running.set(false);
    }
}
