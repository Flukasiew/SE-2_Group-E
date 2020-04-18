package com.pw.server.network;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class GameMasterReaderTest {

    private GameMasterReader gameMasterReader;

    @Mock
    private Socket socket;
    @Mock
    private IOHandler ioHandler;
    @Mock
    private BlockingQueue<String> messages;
    @Mock
    private InputStream inputStream;
    @Mock
    private BufferedReader bufferedReader;

    @Before
    public void prepare() {
        initMocks(this);
        gameMasterReader = new GameMasterReader(socket, ioHandler, messages);
    }

    @Test
    public void shouldStop() throws InterruptedException, IOException {
        doReturn(true).when(bufferedReader).ready();
        doReturn("line").when(bufferedReader).readLine();
        doReturn(inputStream).when(socket).getInputStream();
        doReturn(bufferedReader).when(ioHandler).getReader(inputStream);

        gameMasterReader.start();
        Thread.sleep(100);
        gameMasterReader.stopRunning();
        Thread.sleep(100);

        assertThat(gameMasterReader.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    public void shouldPutGameMasterMessageInQueue() throws IOException, InterruptedException {
        String message = "message";

        doReturn(true).when(bufferedReader).ready();
        doReturn(message).when(bufferedReader).readLine();
        doReturn(inputStream).when(socket).getInputStream();
        doReturn(bufferedReader).when(ioHandler).getReader(inputStream);

        gameMasterReader.start();
        Thread.sleep(50);
        gameMasterReader.stopRunning();

        verify(messages, atLeast(1)).put(message);
    }
}
