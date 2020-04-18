package com.pw.server.factory;

import com.pw.server.model.PlayerMessage;
import com.pw.server.network.GameMasterConnector;
import com.pw.server.network.IOHandler;
import com.pw.server.network.PlayersConnector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class Factory {
    public GameMasterConnector createGameMasterConnector(ServerSocket serverSocket, IOHandler ioHandler,
                                                         BlockingQueue<String> messages) {
        return new GameMasterConnector(serverSocket, ioHandler, messages);
    }

    public PlayersConnector createPlayersConnector(ServerSocket serverSocket, IOHandler ioHandler,
                                                   BlockingQueue<PlayerMessage> messages,
                                                   ConcurrentMap<Integer, PrintWriter> playerWriters) {
        return new PlayersConnector(serverSocket, ioHandler, messages, playerWriters);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }
}
