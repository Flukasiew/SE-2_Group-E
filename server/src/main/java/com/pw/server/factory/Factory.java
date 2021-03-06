package com.pw.server.factory;

import com.pw.server.model.Config;
import com.pw.server.model.PlayerMessage;
import com.pw.server.network.GameMasterConnector;
import com.pw.server.network.IOHandler;
import com.pw.server.network.PlayersConnector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Factory {
    public GameMasterConnector createGameMasterConnector(ServerSocket serverSocket, IOHandler ioHandler,
                                                         BlockingQueue<String> messages, Config config) {
        return new GameMasterConnector(serverSocket, ioHandler, messages, config);
    }

    public PlayersConnector createPlayersConnector(ServerSocket serverSocket, IOHandler ioHandler,
                                                   BlockingQueue<PlayerMessage> messages,
                                                   Map<String, PrintWriter> playerWriters, Config config) {
        return new PlayersConnector(serverSocket, ioHandler, messages, playerWriters, config);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }
}
