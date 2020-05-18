package com.pw.integrationtests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.pw.gamemaster.GameMasterApp;
import com.pw.player.PlayerApp;
import com.pw.server.ServerApp;

import lombok.SneakyThrows;

public class IntegrationTest {

	private static final int playerCount = 8;
	private static final int totalThreadCount = totalThreadCount(playerCount);

	private Runnable server = () -> ServerApp.main(null);
	private Runnable gameMaster = () -> {
		try {
			GameMasterApp.main("0.0.0.0", 1300);
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
	private Runnable player = () -> PlayerApp.main(null);

	@Before
	public void prepare() {
	}

	@SneakyThrows
	@Test
	public void shouldCompleteGame() {
		ExecutorService executor = Executors.newFixedThreadPool(totalThreadCount);
		executor.submit(server);
		executor.submit(gameMaster);
		for (int i = 0; i < playerCount; i++) {
			executor.submit(player);
		}

		executor.awaitTermination(5, TimeUnit.MINUTES);
	}

	private static int totalThreadCount(int playerCount) {
		int serverThreadCount = 5 + playerCount;
		int gameMasterThreadCount = 3;
		int playersThreadCount = playerCount * 2;
		return serverThreadCount + gameMasterThreadCount + playersThreadCount;
	}
}
