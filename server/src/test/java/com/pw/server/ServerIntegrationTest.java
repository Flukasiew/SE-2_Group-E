package com.pw.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.common.dto.GameSetupDTO;
import com.pw.common.dto.GameSetupStatusDTO;
import com.pw.common.dto.PlayerConnectMessageDTO;
import com.pw.common.model.Action;
import com.pw.common.model.GameEndResult;
import com.pw.common.model.GameSetupStatus;
import com.pw.server.network.CommunicationServer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

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
        String playerGuidA = "a";
        String playerGuidB = "b";
        Thread gameMaster = new Thread(() -> {
            try {
                SimpleClient client = new SimpleClient();
                client.startConnection(host, port);
                client.receiveMessage();
                client.sendMessage(MAPPER.writeValueAsString(new GameSetupStatusDTO(Action.setup, GameSetupStatus.OK)));

                assertThat(MAPPER.readValue(client.receiveMessage(), PlayerConnectMessageDTO.class))
                        .isNotNull().extracting(PlayerConnectMessageDTO::getPlayerGuid).isIn(playerGuidA, playerGuidB);
                assertThat(MAPPER.readValue(client.receiveMessage(), PlayerConnectMessageDTO.class)).isNotNull()
                        .isNotNull().extracting(PlayerConnectMessageDTO::getPlayerGuid).isIn(playerGuidA, playerGuidB);

                client.sendMessage(MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.BLUE)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread playerA = new Thread(() -> {
            boolean error = false;
            try {
                SimpleClient client = new SimpleClient();

                Thread.sleep(100);

                client.startConnection(host, port);
                client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, playerGuidA)));
            } catch (Exception e) {
                error = true;
                e.printStackTrace();
            }
            assertThat(error).isFalse();
        });
        Thread playerB = new Thread(() -> {
            boolean error = false;
            try {
                SimpleClient client = new SimpleClient();

                Thread.sleep(100);

                client.startConnection(host, port);
                client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, playerGuidB)));
            } catch (Exception e) {
                error = true;
                e.printStackTrace();
            }
            assertThat(error).isFalse();
        });

        gameMaster.start();
        playerA.start();
        playerB.start();

        communicationServer.listen();

        Thread.sleep(500);

        assertThat(communicationServer.getPlayerWriters().size()).isEqualTo(2);
    }

    @Test
    public void shouldRelayMessageFromPlayerToGameMasterAndBack() throws Exception {
        String playerGuid = "a";
        String messageFromPlayer = "fverbt";
        String messageFromGameMaster = MAPPER.writeValueAsString(Map.of("playerGuid", playerGuid, "message",
                messageFromPlayer));

        Thread gameMaster = new Thread(() -> {
            try {
                SimpleClient client = new SimpleClient();
                client.startConnection(host, port);
                client.receiveMessage();
                client.sendMessage(MAPPER.writeValueAsString(new GameSetupStatusDTO(Action.setup, GameSetupStatus.OK)));
                client.receiveMessage();

                assertThat(client.receiveMessage()).isEqualTo(messageFromPlayer);
                client.sendMessage(messageFromGameMaster);

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
                client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, "a")));

                client.sendMessage(messageFromPlayer);

                assertThat(client.receiveMessage()).isEqualTo(messageFromGameMaster);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        gameMaster.start();
        player.start();

        communicationServer.listen();
    }
}
