package com.pw.server.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.common.model.Action;
import com.pw.common.model.GameEndResult;
import com.pw.server.exception.UnrecognizedMessageException;
import com.pw.server.factory.Factory;
import com.pw.server.model.PlayerMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CommunicationServerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CommunicationServer communicationServer;
    private int port = 1300;
    private String endMessage = MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.BLUE));

    @Mock
    private Factory factory;
    @Mock
    private ServerSocket serverSocket;
    @Mock
    private GameMasterConnector gameMasterConnector;
    @Mock
    private PlayersConnector playersConnector;
    @Mock
    private IOHandler ioHandler;
    @Mock
    private BlockingQueue<PlayerMessage> messagesFromPlayers;
    @Mock
    private BlockingQueue<String> messagesFromGameMaster;
    @Mock
    private PrintWriter gameMasterWriter;
    @Mock
    private Map<String, PrintWriter> playerWriters;

    public CommunicationServerTest() throws JsonProcessingException {
    }

    @Before
    public void prepare() throws Exception {
        initMocks(this);

        communicationServer = new CommunicationServer(port, "localhost");
        communicationServer.setFactory(factory);
        communicationServer.setIoHandler(ioHandler);
        communicationServer.setMessagesFromPlayers(messagesFromPlayers);
        communicationServer.setMessagesFromGameMaster(messagesFromGameMaster);
        communicationServer.setGameMasterWriter(gameMasterWriter);
        communicationServer.setPlayerWriters(playerWriters);

        mockFactory();
        mockGameMasterAndPlayersSetup();
    }

    @Test
    public void shouldEndGame() throws Exception {
        doReturn(true).when(messagesFromPlayers).isEmpty();
        doReturn(false).when(messagesFromGameMaster).isEmpty();
        doReturn(endMessage).when(messagesFromGameMaster).take();

        communicationServer.listen();

        assertThat(communicationServer.getEndGame()).isTrue();
    }

    @Test
    public void shouldRelayMessageFromGameMaster() throws Exception {
        String playerGuid = "a";
        String messageFromGameMaster = MAPPER.writeValueAsString(Map.of("playerGuid", playerGuid));
        PrintWriter printWriter = mock(PrintWriter.class);

        doReturn(true).when(messagesFromPlayers).isEmpty();
        doReturn(false).when(messagesFromGameMaster).isEmpty();
        when(messagesFromGameMaster.take()).thenReturn(messageFromGameMaster).thenReturn(endMessage);

        doReturn(printWriter).when(playerWriters).get(playerGuid);

        communicationServer.listen();

        verify(printWriter).println(messageFromGameMaster);
    }

    @Test
    public void shouldRelayMessageFromPlayer() throws Exception {
        PlayerMessage messageFromPlayer = new PlayerMessage("b", "fwagwerabrweh");

        doReturn(false).when(messagesFromPlayers).isEmpty();
        doReturn(false).when(messagesFromGameMaster).isEmpty();

        doReturn(messageFromPlayer).when(messagesFromPlayers).take();

        doReturn(endMessage).when(messagesFromGameMaster).take();

        communicationServer.listen();

        verify(gameMasterWriter).println(messageFromPlayer.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenNoPlayGuidIsProvidedInMessageFromGameMaster() throws InterruptedException {
        String message = "abc";

        doReturn(true).when(messagesFromPlayers).isEmpty();
        doReturn(false).when(messagesFromGameMaster).isEmpty();

        doReturn(message).when(messagesFromGameMaster).take();

        assertThrows(UnrecognizedMessageException.class, () -> communicationServer.listen());
    }

    private void mockFactory() throws IOException {
        doReturn(serverSocket).when(factory).createServerSocket(port);
        doReturn(gameMasterConnector).when(factory).createGameMasterConnector(serverSocket, ioHandler, messagesFromGameMaster);
        doReturn(playersConnector).when(factory).createPlayersConnector(serverSocket, ioHandler, messagesFromPlayers,
                playerWriters);
    }

    private void mockGameMasterAndPlayersSetup() throws Exception {
        doNothing().when(gameMasterConnector).connect();
        doNothing().when(gameMasterConnector).setup();
        doReturn(gameMasterWriter).when(gameMasterConnector).getWriter();

        doNothing().when(playersConnector).start();
    }
}
