package com.pw.gamemaster;

import com.pw.gamemaster.exception.UnexpectedActionException;
import com.pw.gamemaster.model.GameMaster;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class GameMasterApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMasterApp.class);

    public static void main(String[] args) throws IOException, ParseException, UnexpectedActionException {
        LOGGER.info("GM app started");
        if (args.length != 2) {
            System.out.println("Arguemnts: host_address port_number\nDefault:");
            System.out.println("0.0.0.0 1300");
            LOGGER.error("GM initialization failed");
            return;
        }
        try {
            int port = Integer.parseInt(args[1]);
            String host = args[0];
            Path gmConfigPath = Paths.get("gmconf.json");
            GameMaster gameMaster = new GameMaster(host, port, gmConfigPath);
            gameMaster.listen();
        } catch (NumberFormatException e) {
            LOGGER.error("Error while parsing port number" + e.toString());
        } catch (Exception e) {
            LOGGER.error("Error occured in game master" + e.toString());
        }
        LOGGER.info("GM app finished");
    }
}
