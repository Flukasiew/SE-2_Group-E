package com.pw.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.pw.gamemaster.GameMasterApp;
import com.pw.player.PlayerApp;
import com.pw.server.ServerApp;

import lombok.SneakyThrows;

public class IntegrationTest {

	private static final int playerCount = 8;
	private static final int totalThreadCount = totalThreadCount(playerCount);

	private final Runnable server = () -> ServerApp.main(null);
	private final Runnable gameMaster = () -> {
		try {
			String[] argsGM = new String[] {"0.0.0.0", "1300"};
			GameMasterApp.main(argsGM);
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
	private Runnable player = () -> {
		String argsPlayer[] = new String[] {"0.0.0.0", "1300"};
		PlayerApp.main(argsPlayer);
	};


	@SneakyThrows
	@Test
	public void shouldCompleteGame() {
		ExecutorService executor = Executors.newFixedThreadPool(totalThreadCount);
		executor.submit(server);
		executor.submit(gameMaster);
		Thread.sleep(1000);
		for (int i = 0; i < playerCount; i++) {
			executor.submit(player);
		}

		assertThat(executor.awaitTermination(1, TimeUnit.MINUTES)).isTrue();
	}

	@SneakyThrows
	@Test
	public void shouldEndGameWhenGameMasterDisconnects() {
		ExecutorService executor = Executors.newFixedThreadPool(totalThreadCount);
		executor.submit(server);
		var gameMasterFuture = executor.submit(gameMaster);
		Thread.sleep(1000);
		for (int i = 0; i < playerCount; i++) {
			executor.submit(player);
		}

		Thread.sleep(5000);
		gameMasterFuture.cancel(true);
		assertThat(executor.awaitTermination(10, TimeUnit.SECONDS));
	}

	private static int totalThreadCount(int playerCount) {
		int serverThreadCount = 4 + playerCount;
		int gameMasterThreadCount = 1;
		int playersThreadCount = playerCount;
		return serverThreadCount + gameMasterThreadCount + playersThreadCount;
	}
}
