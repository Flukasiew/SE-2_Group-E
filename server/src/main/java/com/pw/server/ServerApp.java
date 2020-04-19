package com.pw.server;

import com.pw.server.model.Config;
import com.pw.server.network.CommunicationServer;

public class ServerApp {

    public static void main(String[] args) throws Exception {
        Config config = Config.create();

        CommunicationServer communicationServer = new CommunicationServer(1300, "localhost", config);

        communicationServer.listen();
    }
}
