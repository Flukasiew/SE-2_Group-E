package com.pw.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.common.dto.GameSetupDTO;
import com.pw.common.dto.GameSetupStatusDTO;
import com.pw.common.model.Action;
import com.pw.common.model.GameEndResult;
import com.pw.common.model.GameSetupStatus;
import com.pw.server.network.CommunicationServer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerIntegrationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CommunicationServer communicationServer;
    private String host = "localhost";
    private int port = 1300;

    @Before
    public void prepare() {
        communicationServer = new CommunicationServer(port, host);
    }

    @Test
    public void shouldConnectAndSetupGameMaster() throws Exception {
        Thread gameMaster = new Thread(() -> {
            try {
                SimpleClient client = new SimpleClient();
                client.startConnection(host, port);

                assertThat(client.receiveMessage()).isEqualTo(MAPPER.writeValueAsString(new GameSetupDTO(Action.setup)));

                client.sendMessage(MAPPER.writeValueAsString(new GameSetupStatusDTO(Action.setup, GameSetupStatus.OK)));
                client.sendMessage(MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.BLUE)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        gameMaster.start();
        communicationServer.listen();
    }

    @Test
    public void shouldConnectPlayers() throws Exception {
        Thread gameMaster = new Thread(() -> {
            try {
                SimpleClient client = new SimpleClient();
                client.startConnection(host, port);
                client.receiveMessage();
                client.sendMessage(MAPPER.writeValueAsString(new GameSetupStatusDTO(Action.setup, GameSetupStatus.OK)));

                Thread.sleep(300);

                client.sendMessage(MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.BLUE)));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        Runnable playerRunnable = () -> {
            boolean error = false;
            try {
                SimpleClient client = new SimpleClient();

                Thread.sleep(100);

                client.startConnection(host, port);
            } catch (Exception e) {
                error = true;
                e.printStackTrace();
            }
            assertThat(error).isFalse();
        };
        Thread player1 = new Thread(playerRunnable);
        Thread player2 = new Thread(playerRunnable);

        gameMaster.start();
        player1.start();
        player2.start();

        communicationServer.listen();

        Thread.sleep(300);

        assertThat(communicationServer.getPlayerWriters().size()).isEqualTo(2);
    }

    @Test
    public void shouldRelayMessageFromPlayerToGameMasterAndBack() throws Exception {
        String message = "fverbt";

        Thread gameMaster = new Thread(() -> {
            try {
                SimpleClient client = new SimpleClient();
                client.startConnection(host, port);
                client.receiveMessage();
                client.sendMessage(MAPPER.writeValueAsString(new GameSetupStatusDTO(Action.setup, GameSetupStatus.OK)));

                assertThat(client.receiveMessage()).isEqualTo(message);
                client.sendMessage(message);

                Thread.sleep(200);

                client.sendMessage(MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.BLUE)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread player = new Thread(() -> {
            try {
                SimpleClient client = new SimpleClient();

                Thread.sleep(100);

                client.startConnection(host, port);

                client.sendMessage(message);

                assertThat(client.receiveMessage()).isEqualTo(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        gameMaster.start();
        player.start();

        communicationServer.listen();
    }
}
