package com.pw.integrationtests;

import com.pw.gamemaster.GameMasterApp;
import com.pw.player.PlayerApp;
import com.pw.server.ServerApp;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest {

    @Test
    public void shouldCompleteGame() throws InterruptedException {
        int playersCount = 8;

        Thread server = new Thread(() -> ServerApp.main(null));
        Thread gameMaster = new Thread(() -> {
            try {
                GameMasterApp.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        List<Thread> players = IntStream.range(0, playersCount)
                .mapToObj(i -> new Thread(() -> PlayerApp.main(null)))
                .collect(toList());

        server.start();
        Thread.sleep(200);
        gameMaster.start();
        Thread.sleep(200);
        players.forEach(Thread::start);

        while(server.isAlive()) {
            Thread.sleep(200);
        }
        Thread.sleep(200);

        assertThat(gameMaster.isAlive()).isFalse();
        players.forEach(player -> assertThat(player.isAlive()).isFalse());
    }
}
