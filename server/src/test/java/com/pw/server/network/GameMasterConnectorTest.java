package com.pw.server.network;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pw.server.exception.GameMasterSetupException;
import com.pw.server.model.Config;

public class GameMasterConnectorTest {

    private GameMasterConnector gameMasterConnector;

    private Config config = new Config();

    @Mock
    private ServerSocket serverSocket;
    @Mock
    private IOHandler ioHandler;
    @Mock
    private BlockingQueue<String> messages;
    @Mock
    private Socket socket;

    @Before
    public void prepare() {
        initMocks(this);
        gameMasterConnector = new GameMasterConnector(serverSocket, ioHandler, messages, config);
    }

    @Test
    public void shouldConnectGameMasterAndSetIOHandlers() throws Exception {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);

        doReturn(outputStream).when(socket).getOutputStream();
        doReturn(inputStream).when(socket).getInputStream();

        doReturn(socket).when(serverSocket).accept();

        doReturn(mock(PrintWriter.class)).when(ioHandler).getWriter(outputStream);
        doReturn(mock(BufferedReader.class)).when(ioHandler).getReader(inputStream);

        gameMasterConnector.connect();

        assertThat(gameMasterConnector.getReader()).isNotNull();
        assertThat(gameMasterConnector.getWriter()).isNotNull();
    }

    @Test
    public void shouldThrowExceptionWhenAcceptingConnectionsTimedOut() throws Exception {
        doThrow(new SocketTimeoutException()).when(serverSocket).accept();

        assertThrows(GameMasterSetupException.class, () -> gameMasterConnector.connect());
    }
}
