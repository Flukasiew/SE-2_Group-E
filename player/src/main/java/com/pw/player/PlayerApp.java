package com.pw.player;

import com.pw.player.model.*;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerApp.class);

    public static void main(String[] args) {
        LOGGER.info("Player app started");
        if (args.length != 2) {
            System.out.println("Arguemnts: host_address port_number\nDefault:");
            System.out.println("0.0.0.0 1300");
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
        LOGGER.info("Player app finished");
    }
}