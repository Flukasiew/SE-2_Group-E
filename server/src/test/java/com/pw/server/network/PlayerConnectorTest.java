package com.pw.server.network;

import com.pw.server.model.PlayerMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PlayerConnectorTest {

    PlayersConnector playerConnector;

    @Mock
    private ServerSocket serverSocket;

    @Mock
    private IOHandler ioHandler;

    @Mock
    private BlockingQueue<PlayerMessage> messages;

    @Mock
    private ConcurrentMap<Integer, PrintWriter> playerWriters;

    @Mock
    private Socket socket;

    @Mock
    private OutputStream outputStream;

    @Before
    public void prepare() {
        initMocks(this);
        playerConnector = new PlayersConnector(serverSocket, ioHandler, messages, playerWriters);
    }

    @Test
    public void shouldStopThread() throws InterruptedException, IOException {
        doReturn(outputStream).when(socket).getOutputStream();
        doReturn(mock(PrintWriter.class)).when(ioHandler).getWriter(outputStream);
        doReturn(socket).when(serverSocket).accept();

        playerConnector.start();
        Thread.sleep(100);
        playerConnector.stopRunning();
        Thread.sleep(100);

        assertThat(playerConnector.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    public void shouldAcceptPlayerConnectionAndLaunchReader() throws IOException {
        doReturn(outputStream).when(socket).getOutputStream();
        doReturn(mock(PrintWriter.class)).when(ioHandler).getWriter(outputStream);
        doReturn(socket).when(serverSocket).accept();
        when(serverSocket.accept()).thenReturn(socket).thenThrow(new SocketException());

        int activeThreadsAtStart = Thread.activeCount();
        playerConnector.start();

        assertThat(Thread.activeCount()).isEqualTo(activeThreadsAtStart + 1);
    }

    @Test
    public void shouldPutPrintWriterIntoPlayersMap() throws IOException {
        PrintWriter printWriter = mock(PrintWriter.class);

        doReturn(outputStream).when(socket).getOutputStream();
        doReturn(printWriter).when(ioHandler).getWriter(outputStream);
        doReturn(socket).when(serverSocket).accept();
        when(serverSocket.accept()).thenReturn(socket).thenThrow(new SocketException());

        playerConnector.run();

        verify(playerWriters).put(1, printWriter);
    }
}
