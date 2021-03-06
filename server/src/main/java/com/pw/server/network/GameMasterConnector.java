package com.pw.server.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.server.exception.GameMasterSetupException;
import com.pw.server.model.Config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameMasterConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameMasterConnector.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ServerSocket serverSocket;
	private IOHandler ioHandler;
	private Socket gameMasterSocket;
	private BlockingQueue<String> messages;
	private Config config;

	private PrintWriter writer;
	private BufferedReader reader;

	private GameMasterReader gameMasterReader;

	public GameMasterConnector(ServerSocket serverSocket, IOHandler ioHandler, BlockingQueue<String> messages,
			Config config) {
		this.serverSocket = serverSocket;
		this.ioHandler = ioHandler;
		this.messages = messages;
		this.config = config;

		try {
			serverSocket.setSoTimeout(config.getGameMasterConnectionTimeout());
		} catch (SocketException e) {
			LOGGER.error(e.getLocalizedMessage());
		}
	}

	public void connect() throws GameMasterSetupException, IOException {
		LOGGER.info("Accepting game master connection...");
		try {
			gameMasterSocket = serverSocket.accept();
		} catch (IOException e) {
			throw new GameMasterSetupException("Accepting game master connection interrupted", e);
		}
		LOGGER.info("Connection with game master established");

		writer = ioHandler.getWriter(gameMasterSocket.getOutputStream());
		reader = ioHandler.getReader(gameMasterSocket.getInputStream());

		gameMasterReader = new GameMasterReader(gameMasterSocket, ioHandler, messages);
		gameMasterReader.start();
	}

	public void stopRunning() {
		if (gameMasterReader != null) {
			gameMasterReader.stopRunning();
		}

		try {
			reader.close();
			writer.close();
			gameMasterSocket.close();
		} catch (IOException e) {
			LOGGER.error("Unable to close game master connector", e);
		}
	}
}
