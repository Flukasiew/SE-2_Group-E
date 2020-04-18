package com.pw.server;

import com.pw.server.network.CommunicationServer;

public class ServerApp {

    public static void main(String[] args) throws Exception {
        CommunicationServer communicationServer = new CommunicationServer(1300, "localhost");

        communicationServer.listen();
    }
}
