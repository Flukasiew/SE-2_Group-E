package com.pw.gamemaster;

import com.pw.gamemaster.model.GameMaster;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class GameMasterApp {
	public static void main(String[] args) throws IOException, ParseException {
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
		GameMaster xd = new GameMaster();
		xd.loadConfigurationFromJson("gmconf.json");
		String path = "C:/Users/sz/IdeaProjects/SE-2_Group-E/test.json";
		xd.saveConfigurationToJson(path);
	}
}
