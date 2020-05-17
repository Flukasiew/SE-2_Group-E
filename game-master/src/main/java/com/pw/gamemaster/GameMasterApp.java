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

	private static final Logger LOGGER = LoggerFactory.getLogger(GameMaster.class);
	public static void main(String host, int port) throws IOException, ParseException, UnexpectedActionException {
		//System.out.println("XD");
		/*JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("gmconf.json"));
		Long value = (Long) jsonObject.get("delayMove");
		System.out.println(value);
		JSONArray jsonArray = (JSONArray)jsonObject.get("predefinedGoalPositions");
		Iterator iterator = jsonArray.iterator();
		while(iterator.hasNext()) {
			JSONArray elem = (JSONArray)iterator.next();
			System.out.println(((Long)elem.get(0)).intValue());
		}
		double meh = (Double)jsonObject.get("shamProbability");
		int meh2 = ((Long)jsonObject.get("maxTeamSize")).intValue();
		System.out.println(meh2);*/
		String path = "gmconf.json";
		Path p1 = Paths.get("gmconf.json");
		GameMaster xd = new GameMaster(host, port, p1);
//		String path = "gmconf.json";
//		Path p1 = Paths.get("gmconf.json");
//		System.out.println(p1.toAbsolutePath().toString());
//		xd.loadConfigurationFromJson(p1.toString());
//		LOGGER.info("configuration loaded");
		LOGGER.info("app finished");
	}
}
