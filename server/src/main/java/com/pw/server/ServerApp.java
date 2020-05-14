package com.pw.server;

import com.pw.server.model.Config;
import com.pw.server.network.CommunicationServer;

public class ServerApp {

    public static void main(String[] args) {
        Config config = Config.create();

        CommunicationServer communicationServer = new CommunicationServer(config);
        communicationServer.listen();
    }
}
