package com.pw.server;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.common.dto.PlayerConnectMessageDTO;
import com.pw.common.model.Action;
import com.pw.common.model.GameEndResult;
import com.pw.server.model.Config;
import com.pw.server.network.CommunicationServer;

public class ServerIntegrationTest {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private CommunicationServer communicationServer;

	private Config config = Config.create();
	private String host = config.getIpAddress();
	private int port = config.getPortNumber();

	@Before
	public void prepare() {
		communicationServer = new CommunicationServer(config);
	}

	@Test
	public void shouldConnectAndSetupGameMaster() {
		Thread gameMaster = new Thread(() -> {
			try {
				SimpleClient client = new SimpleClient();
				client.startConnection(host, port);

				client.sendMessage(MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.Blue)));
				client.stopConnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		ExecutorService executor = Executors.newFixedThreadPool(20);
		Future<?> future = executor.submit(gameMaster);
		communicationServer.listen();

		assertThat(future.isDone()).isTrue();
	}

	@Test
	public void shouldConnectPlayers() throws Exception {
		String playerGuidA = "a";
		String playerGuidB = "b";
		Thread gameMaster = new Thread(() -> {
			try {
				SimpleClient client = new SimpleClient();
				client.startConnection(host, port);

				assertThat(MAPPER.readValue(client.receiveMessage(), PlayerConnectMessageDTO.class))
						.isNotNull().extracting(PlayerConnectMessageDTO::getPlayerGuid).isIn(playerGuidA, playerGuidB);
				assertThat(MAPPER.readValue(client.receiveMessage(), PlayerConnectMessageDTO.class)).isNotNull()
						.isNotNull().extracting(PlayerConnectMessageDTO::getPlayerGuid).isIn(playerGuidA, playerGuidB);

				client.sendMessage(MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.Blue)));
				client.stopConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Thread playerA = new Thread(() -> {
			boolean error = false;
			try {
				SimpleClient client = new SimpleClient();
				Thread.sleep(500);

				client.startConnection(host, port);
				client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port,
						playerGuidA)));

				client.stopConnection();
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
				Thread.sleep(500);

				client.startConnection(host, port);
				client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port,
						playerGuidB)));

				client.stopConnection();
			} catch (Exception e) {
				error = true;
				e.printStackTrace();
			}
			assertThat(error).isFalse();
		});

		ExecutorService executor = Executors.newFixedThreadPool(20);

		List<Future<?>> futures = newArrayList(
				executor.submit(gameMaster),
				executor.submit(playerA),
				executor.submit(playerB));

		communicationServer.listen();

		Thread.sleep(500);

		assertThat(communicationServer.getPlayerWriters().size()).isEqualTo(2);
		futures.forEach(future -> assertThat(future.isDone()).isTrue());
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
				assertThat(client.receiveMessage()).isEqualTo(messageFromPlayer);
				client.sendMessage(messageFromGameMaster);

				Thread.sleep(200);

				client.sendMessage(MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, GameEndResult.Blue)));
				client.stopConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		Thread player = new Thread(() -> {
			try {
				SimpleClient client = new SimpleClient();
				Thread.sleep(500);
				client.startConnection(host, port);
				client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port, "a")));
				client.sendMessage(messageFromPlayer);
				assertThat(client.receiveMessage()).isEqualTo(messageFromGameMaster);
				client.stopConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		ExecutorService executor = Executors.newFixedThreadPool(20);
		List<Future<?>> futures = newArrayList(
				executor.submit(gameMaster),
				executor.submit(player));

		communicationServer.listen();
		futures.forEach(future -> assertThat(future.isDone()).isTrue());
	}

	@Test
	public void shouldShutDownWhenUnableToMessageGameMaster() throws InterruptedException {
		String playerGuid = "a";
		Thread gameMaster = new Thread(() -> {
			try {
				SimpleClient client = new SimpleClient();
				client.startConnection(host, port);
				client.stopConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Thread player = new Thread(() -> {
			boolean error = false;
			try {
				SimpleClient client = new SimpleClient();

				Thread.sleep(500);

				client.startConnection(host, port);
				client.sendMessage(MAPPER.writeValueAsString(new PlayerConnectMessageDTO(Action.connect, port,
						playerGuid)));

				Thread.sleep(100);

				client.sendMessage("FWEFWEVEVWVQ");
				client.stopConnection();
			} catch (Exception e) {
				error = true;
				e.printStackTrace();
			}
			assertThat(error).isFalse();
		});

		ExecutorService executor = Executors.newFixedThreadPool(20);
		List<Future<?>> futures = newArrayList(
				executor.submit(gameMaster),
				executor.submit(player));

		communicationServer.listen();

		Thread.sleep(500);

		assertThat(communicationServer.getAbruptShutdown()).isTrue();
		futures.forEach(future -> assertThat(future.isDone()).isTrue());
	}
}
