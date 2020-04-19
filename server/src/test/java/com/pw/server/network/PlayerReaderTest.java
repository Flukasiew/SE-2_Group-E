package com.pw.server.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.PlayerConnectMessageDTO;
import com.pw.common.model.Action;
import com.pw.server.exception.PlayerSetupException;
import com.pw.server.model.PlayerMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PlayerReaderTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PlayerReader playerReader;
    private int portNumber = 1300;
    private String playerGuid = "a";

    private String connectMessage = MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, portNumber, playerGuid));

    @Mock
    private Socket socket;
    @Mock
    private IOHandler ioHandler;
    @Mock
    private BlockingQueue<PlayerMessage> messages;
    @Mock
    private Map<String, PrintWriter> playerWriters;
    @Mock
    private InputStream inputStream;
    @Mock
    private BufferedReader reader;
    @Mock
    private OutputStream outputStream;
    @Mock
    private PrintWriter writer;

    public PlayerReaderTest() throws JsonProcessingException {
    }

    @Before
    public void prepare() throws IOException {
        initMocks(this);
        playerReader = new PlayerReader(socket, ioHandler, messages, playerWriters);

        mockIOHandler();
    }

    @Test
    public void shouldStop() throws InterruptedException, IOException {
        doReturn(true).when(reader).ready();
        doReturn("line").when(reader).readLine();

        playerReader.start();
        Thread.sleep(100);
        playerReader.stopRunning();
        Thread.sleep(100);

        assertThat(playerReader.getState()).isEqualTo(Thread.State.TERMINATED);
    }

    @Test
    public void shouldSetupPlayer() throws IOException, InterruptedException {
        doReturn(connectMessage).when(reader).readLine();

        playerReader.start();
        Thread.sleep(100);
        playerReader.stopRunning();

        assertThat(playerReader.getPlayerGuid()).isEqualTo(playerGuid);
        verify(playerWriters).put(playerGuid, writer);
        verify(messages).put(new PlayerMessage(playerGuid, connectMessage));
    }

    @Test
    public void shouldThrowPlayerSetupExceptionWhenConnectMessageIsIncorrect() throws IOException {
        String incorrectMessage = MAPPER.writeValueAsString(Map.of("incorrect", "message"));
        doReturn(incorrectMessage).when(reader).readLine();

        assertThrows(PlayerSetupException.class, () -> playerReader.run());
    }

    @Test
    public void shouldPutPlayerMessageInQueue() throws IOException, InterruptedException {
        String message = "message";

        doReturn(true).when(reader).ready();
        when(reader.readLine()).thenReturn(connectMessage).thenReturn(message);

        playerReader.start();
        Thread.sleep(100);
        playerReader.stopRunning();

        verify(messages, atLeast(1)).put(new PlayerMessage(playerGuid, message));
    }

    private void mockIOHandler() throws IOException {
        doReturn(inputStream).when(socket).getInputStream();
        doReturn(reader).when(ioHandler).getReader(inputStream);

        doReturn(outputStream).when(socket).getOutputStream();
        doReturn(writer).when(ioHandler).getWriter(outputStream);
    }
}
