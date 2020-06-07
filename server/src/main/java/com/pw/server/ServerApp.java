package com.pw.server;

import com.pw.server.model.Config;
import com.pw.server.network.CommunicationServer;

public class ServerApp {

    public static void main(String[] args) {
        Config config = args.length == 1 ? Config.createFrom(args[0]) : new Config();

        CommunicationServer communicationServer = new CommunicationServer(config);
        communicationServer.listen();
    }
}
