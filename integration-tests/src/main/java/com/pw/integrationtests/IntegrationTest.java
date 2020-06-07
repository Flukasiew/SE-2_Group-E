package com.pw.integrationtests;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.pw.gamemaster.GameMasterApp;
import com.pw.player.PlayerApp;
import com.pw.server.ServerApp;

import lombok.SneakyThrows;

public class IntegrationTest {

	private static final int playerCount = 8;
	private static final int totalThreadCount = totalThreadCount(playerCount);

	private final Runnable server = () -> ServerApp.main(new String[] { Objects.requireNonNull(
			IntegrationTest.class.getClassLoader().getResource("server_config.json")).getFile() });
	private final Runnable gameMaster = () -> {
		try {
			String[] argsGM = new String[] { "0.0.0.0", "1300" };
			GameMasterApp.main(argsGM);
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
	private Runnable player = () -> {
		String argsPlayer[] = new String[] { "0.0.0.0", "1300" };
		PlayerApp.main(argsPlayer);
	};

	@SneakyThrows
	@Test
	public void shouldCompleteGame() {
		ExecutorService executor = Executors.newFixedThreadPool(totalThreadCount);
		List<Future<?>> futures = newArrayList(
				executor.submit(server),
				executor.submit(gameMaster));
		Thread.sleep(2000);
		for (int i = 0; i < playerCount; i++) {
			futures.add(executor.submit(player));
		}

		executor.awaitTermination(1, TimeUnit.MINUTES);
		System.out.println("d");
	}

	@SneakyThrows
	//	@Test
	//	public void shouldEndGameWhenGameMasterDisconnects() {
	//		ExecutorService executor = Executors.newFixedThreadPool(totalThreadCount);
	//		executor.submit(server);
	//		var gameMasterFuture = executor.submit(gameMaster);
	//		Thread.sleep(1000);
	//		for (int i = 0; i < playerCount; i++) {
	//			executor.submit(player);
	//		}
	//
	//		Thread.sleep(5000);
	//		gameMasterFuture.cancel(true);
	//		assertThat(executor.awaitTermination(1, TimeUnit.MINUTES)).isTrue();
	//	}

	private static int totalThreadCount(int playerCount) {
		int serverThreadCount = 4 + playerCount;
		int gameMasterThreadCount = 1;
		int playersThreadCount = playerCount;
		return serverThreadCount + gameMasterThreadCount + playersThreadCount;
	}
}
