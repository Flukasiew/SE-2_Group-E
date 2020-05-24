package com.pw.server.network;

import static java.lang.String.format;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.common.dto.GameMessageEndDTO;
import com.pw.common.model.Action;
import com.pw.server.exception.GameMasterConnectionException;
import com.pw.server.exception.GameMasterSetupException;
import com.pw.server.exception.UnrecognizedMessageException;
import com.pw.server.factory.Factory;
import com.pw.server.model.Config;
import com.pw.server.model.PlayerMessage;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Getter
@Setter
public class CommunicationServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationServer.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String PLAYER_GUID = "playerGuid";

	private int portNumber;
	private String ipAddress;
	private Config config;

	private Factory factory = new Factory();

	private BlockingQueue<PlayerMessage> messagesFromPlayers = new LinkedBlockingQueue<>();
	private BlockingQueue<String> messagesFromGameMaster = new LinkedBlockingQueue<>();

	private PlayersConnector playersConnector;
	private GameMasterConnector gameMasterConnector;

	private IOHandler ioHandler = new IOHandler();
	private PrintWriter gameMasterWriter;
	private Map<String, PrintWriter> playerWriters = new ConcurrentHashMap<>();

	private Boolean stopServer = false;

	public CommunicationServer(Config config) {
		this.portNumber = config.getPortNumber();
		this.ipAddress = config.getIpAddress();
		this.config = config;
	}

	public void listen() {
		LOGGER.info("Listening started");

		try {
			ServerSocket serverSocket = factory.createServerSocket(portNumber);
			setupGameMaster(serverSocket);
			setupPlayers(serverSocket);
			relayMessages();
		} catch (Exception e) {
			LOGGER.error("Abruptly stopping server...", e);
			sendAbruptShutdownMessage();
		}

		close();
		LOGGER.info("Listening ended");
	}

	private void setupGameMaster(ServerSocket serverSocket)
			throws IOException, GameMasterSetupException, InterruptedException {
		gameMasterConnector = factory.createGameMasterConnector(serverSocket, ioHandler, messagesFromGameMaster,
				config);
		gameMasterConnector.connect();
		gameMasterConnector.setup();
		gameMasterWriter = gameMasterConnector.getWriter();

		LOGGER.info("Game master setup completed");
	}

	private void setupPlayers(ServerSocket serverSocket) {
		playersConnector = factory.createPlayersConnector(serverSocket, ioHandler, messagesFromPlayers, playerWriters
				, config);
		playersConnector.start();

		LOGGER.info("Players setup initiated");
	}

	public void relayMessages() throws InterruptedException, GameMasterConnectionException {
		LOGGER.info("Relaying messages started");

		while (!stopServer) {
			if (!messagesFromPlayers.isEmpty()) {
				handleMessageFromPlayer(messagesFromPlayers.take());
			}
			if (!messagesFromGameMaster.isEmpty()) {
				try {
					handleMessageFromGameMaster(messagesFromGameMaster.take());
				} catch (UnrecognizedMessageException e) {
					LOGGER.error("Unrecognized message from game master", e);
				}
			}
			if (Thread.interrupted()) {
				LOGGER.info("Thread interrupted, abruptly shutting down...");
				throw new InterruptedException();
			}
		}

		LOGGER.info("Relaying messages ended");
	}

	private void handleMessageFromPlayer(PlayerMessage message) throws GameMasterConnectionException {
		LOGGER.info("Handling {} from player", message);
		try {
			sendMessageToGameMasterWithRetries(message.getMessage(), config.getRetriesLimit());
		} catch (GameMasterConnectionException e) {
			LOGGER.error("Game master connection exception, abruptly shutting down...", e);
			throw e;
		}
	}

	private void handleMessageFromGameMaster(String message) throws UnrecognizedMessageException {
		LOGGER.info("Handling {} from game master", message);

		GameMessageEndDTO gameMessageEndDTO = getEndGameMessage(message);
		if (gameMessageEndDTO != null) {
			endGame(gameMessageEndDTO);
			return;
		}

		Map<String, Object> messageJson;
		try {
			messageJson = MAPPER.readValue(message, Map.class);
		} catch (Exception e) {
			throw new UnrecognizedMessageException("Cannot parse JSON", e);
		}

		if (!messageJson.containsKey(PLAYER_GUID) || !(messageJson.get(PLAYER_GUID) instanceof String)) {
			throw new UnrecognizedMessageException("Message does not contain player guid");
		}

		String playerGuid = (String) messageJson.get(PLAYER_GUID);

		PrintWriter playerWriter = playerWriters.get(playerGuid);
		sendMessageToPlayerWithRetries(playerGuid, playerWriter, message, config.getRetriesLimit());
	}

	private GameMessageEndDTO getEndGameMessage(String message) {
		GameMessageEndDTO gameMessageEndDTO;
		try {
			gameMessageEndDTO = MAPPER.readValue(message, GameMessageEndDTO.class);
		} catch (Exception e) {
			return null;
		}
		return gameMessageEndDTO;
	}

	private void endGame(GameMessageEndDTO gameMessageEndDTO) {
		stopServer = true;

		String endMessage;
		try {
			endMessage = MAPPER.writeValueAsString(gameMessageEndDTO);
		} catch (Exception e) {
			LOGGER.error("Unable to parse end game message", e);
			return;
		}

		LOGGER.info("Sending end game messages to players...");
		playerWriters.forEach((playerGuid, playerWriter) -> sendMessageToPlayerWithRetries(playerGuid, playerWriter,
				endMessage, config.getRetriesLimit()));
	}

	private void sendMessageToPlayerWithRetries(String playerGuid, PrintWriter playerWriter, String message,
			int retries) {
		if (playerWriter != null) {
			playerWriter.println(message);
			if (playerWriter.checkError()) {
				if (retries <= 0) {
					LOGGER.error("Unable to relay {} to player {}, breaking connection...", message, playerGuid);
					playerWriter.close();
					playerWriters.remove(playerGuid);
				} else {
					LOGGER.info("Unable to relay message to player {}, {} retries left", playerGuid, retries);
					sendMessageToPlayerWithRetries(playerGuid, playerWriter, message, retries - 1);
				}
			} else {
				LOGGER.info("Message sent to player {}", playerGuid);
			}
		} else {
			LOGGER.error("No writer for {} player", playerGuid);
		}
	}

	private void sendMessageToGameMasterWithRetries(String message, int retries) throws GameMasterConnectionException {
		if (gameMasterWriter != null) {
			gameMasterWriter.println(message);
			if (gameMasterWriter.checkError()) {
				if (retries <= 0) {
					throw new GameMasterConnectionException(
							format("Unable to send %s message to game master", message));
				} else {
					LOGGER.error("Unable to relay message to game master, {} retries left...", retries);
					sendMessageToGameMasterWithRetries(message, retries - 1);
				}
			} else {
				LOGGER.info("Message sent to game master");
			}
		} else {
			LOGGER.error("No writer for game master");
		}
	}

	@SneakyThrows
	private void sendAbruptShutdownMessage() {
		String message = MAPPER.writeValueAsString(new GameMessageEndDTO(Action.end, null));

		playerWriters.forEach((playerGuid, playerWriter) -> sendMessageToPlayerWithRetries(playerGuid, playerWriter,
				message, config.getRetriesLimit()));
		try {
			sendMessageToGameMasterWithRetries(message, config.getRetriesLimit());
		} catch (GameMasterConnectionException ignored) {}
		LOGGER.info("Abrupt shutdown messages sent");
	}

	private void close() {
		if (gameMasterConnector != null) {
			gameMasterConnector.stopRunning();
		}
		if (playersConnector != null) {
			playersConnector.stopRunning();
		}
	}
}
