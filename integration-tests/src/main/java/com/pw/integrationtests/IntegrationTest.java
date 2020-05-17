package com.pw.integrationtests;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.pw.gamemaster.GameMasterApp;
import com.pw.player.PlayerApp;
import com.pw.server.ServerApp;

import lombok.SneakyThrows;

public class IntegrationTest {

	private int playersCount = 0;
	private Thread server;
	private Thread gameMaster;
	private List<Thread> players;

	@Before
	public void prepare() {
		server = new Thread(() -> ServerApp.main(null));
		gameMaster = new Thread(() -> {
			try {
				GameMasterApp.main("0.0.0.0",1300);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		players = IntStream.range(0, playersCount)
				.mapToObj(i -> new Thread(() -> PlayerApp.main(null)))
				.collect(toList());
	}

	@Test
	public void shouldCompleteGame() throws InterruptedException {
		startThreads();

		while (server.isAlive()) {
			Thread.sleep(200);
		}
		Thread.sleep(200);

		assertThat(gameMaster.isAlive()).isFalse();
		players.forEach(player -> assertThat(player.isAlive()).isFalse());
	}

	@Test
	public void shouldEndGameWhenUnableReachGameMaster() throws InterruptedException {
		startThreads();

		Thread.sleep(2000);

		gameMaster.stop();

		Thread.sleep(200);

		assertThat(server.isAlive()).isFalse();
		players.forEach(player -> assertThat(player.isAlive()).isFalse());
	}

	@Test
	public void shouldContinueGameWhenPlayerDisconnects() throws InterruptedException {
		startThreads();

		players.get(0).stop();

		Thread.sleep(200);

		assertThat(server.isAlive()).isTrue();
		assertThat(gameMaster.isAlive()).isTrue();
		for (int i = 1; i < players.size(); i++) {
			assertThat(players.get(i).isAlive()).isTrue();
		}
	}

	@SneakyThrows
	private void startThreads() throws InterruptedException {
		server.start();
		Thread.sleep(200);
		gameMaster.start();
		Thread.sleep(200);
		players.forEach(Thread::start);
	}
}
