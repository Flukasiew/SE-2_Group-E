package com.pw.gamemaster.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pw.gamemaster.exception.UnexpectedActionException;
import com.pw.gamemaster.model.GameMaster;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameMasterTest {
    GameMaster gm = new GameMaster();
    String path = "gmconf.json";
    String pathWrongGMConf = "test.json";

    @Test
    void messageHandlerSetupOK() throws IOException, ParseException, JSONException, UnexpectedActionException  {
        gm.loadConfigurationFromJson(path);
        JSONObject returnMessage = new JSONObject();

        JSONObject setupMessage = new JSONObject();
        setupMessage.put("action", "setup");
        returnMessage = gm.messageHandler(String.valueOf(setupMessage));
        setupMessage.put("status", "OK");
        System.out.println(returnMessage.toJSONString());
        JSONAssert.assertEquals(String.valueOf(setupMessage), String.valueOf(returnMessage), false);
        //assertNull(null);
    }

    @Test
    void messageHandlerSetupNotOK() throws JSONException, ParseException, JsonProcessingException, UnexpectedActionException  {
        JSONObject returnMessage = new JSONObject();

        JSONObject setupMessage = new JSONObject();
        setupMessage.put("action", "setup");
        returnMessage = gm.messageHandler(String.valueOf(setupMessage));
        setupMessage.put("status", "DENIED");
        System.out.println(returnMessage.toJSONString());
        JSONAssert.assertEquals(String.valueOf(setupMessage), String.valueOf(returnMessage), false);
    }

    @Test
    void messageHandlerConnectPlayerOK() throws IOException, ParseException, JSONException, UnexpectedActionException  {
        gm.loadConfigurationFromJson(path);
        JSONObject connectMsg = new JSONObject();
        connectMsg.put("action", "connect");
        connectMsg.put("portNumber", "2020");
        UUID tmpUuid = UUID.randomUUID();
        connectMsg.put("playerGuid", tmpUuid.toString());
        System.out.println(connectMsg);
        JSONObject returnMessage = gm.messageHandler(String.valueOf(connectMsg));
        connectMsg.put("status", "OK");
        System.out.println(returnMessage);
        JSONAssert.assertEquals(String.valueOf(connectMsg), String.valueOf(returnMessage), false);
    }

    @Test
    void messageHandlerConnectPlayerNotOK() throws IOException, ParseException, JSONException, UnexpectedActionException  {
        gm.loadConfigurationFromJson(pathWrongGMConf);
        JSONObject connectMsg = new JSONObject();
        connectMsg.put("action", "connect");
        connectMsg.put("portNumber", "2020");
        UUID tmpUuid = UUID.randomUUID();
        connectMsg.put("playerGuid", tmpUuid.toString());
        //System.out.println(connectMsg);
        gm.messageHandler(String.valueOf(connectMsg));
        tmpUuid = UUID.randomUUID();
        connectMsg.remove("playerGuid");
        connectMsg.put("playerGuid", tmpUuid.toString());
        gm.messageHandler(String.valueOf(connectMsg));
        tmpUuid = UUID.randomUUID();
        connectMsg.remove("playerGuid");
        connectMsg.put("playerGuid", tmpUuid.toString());
        JSONObject returnMsg = gm.messageHandler(String.valueOf(connectMsg));
        connectMsg.put("status", "DENIED");
        System.out.println(returnMsg);
        JSONAssert.assertEquals(String.valueOf(connectMsg), String.valueOf(returnMsg), false);
    }

    @Test
    void messageHandlerReadyNotOK() throws IOException, ParseException, JSONException, UnexpectedActionException  {
        gm.loadConfigurationFromJson(path);
        JSONObject readyMsg = new JSONObject();
        readyMsg.put("action", "ready");
        UUID uuid = UUID.randomUUID();
        readyMsg.put("playerGuid", uuid.toString());
        JSONObject returnMsg = gm.messageHandler(String.valueOf(readyMsg));
        readyMsg.put("status", "NO");

        JSONAssert.assertEquals(String.valueOf(readyMsg), String.valueOf(returnMsg), false);
    }

    @Test
    void messageHandlerReadyOK() throws IOException, ParseException, JSONException, UnexpectedActionException {
        gm.loadConfigurationFromJson(path);
        JSONObject readyMsg = new JSONObject();
        readyMsg.put("action", "ready");
        JSONObject connectMsg = new JSONObject();
        connectMsg.put("action", "connect");
        connectMsg.put("portNumber", "2020");
        UUID uuid = UUID.randomUUID();
        connectMsg.put("playerGuid", uuid.toString());
        readyMsg.put("playerGuid", uuid.toString());
        gm.messageHandler(String.valueOf(connectMsg));
        JSONObject returnMsg = gm.messageHandler(String.valueOf(readyMsg));
        readyMsg.put("status", "YES");
        System.out.println(readyMsg.toJSONString());
        System.out.println(returnMsg.toJSONString());
        JSONAssert.assertEquals(String.valueOf(readyMsg), String.valueOf(returnMsg), false);
    }


}