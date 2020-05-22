package com.pw.player;

import com.pw.player.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(Player.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.error("Player initialization failed");
            return;
        }
        try {
            int port = Integer.parseInt(args[1]);
            String host = args[0];
            Player player = new Player(host, port);
            player.listen();

        } catch (NumberFormatException e) {
            LOGGER.error("Error while parsing port number" + e.toString());
        } catch (Exception e) {
			LOGGER.error("Error occured in player" + e.toString());
		}
    }
}