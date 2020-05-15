package com.pw.server.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pw.server.ServerApp;

import lombok.Data;

@Data
public class Config {
	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

	private Integer portNumber = 1300;
	private String ipAddress = "0.0.0.0";

	private Integer playerConnectionsLimit = 16;

	private Integer gameMasterConnectionTimeout = 5 * 60 * 1000;
	private Integer playerConnectionTimeout = 5 * 60 * 1000;

	private Integer retriesLimit = 5;

	private Config() {
	}

	public static Config create() {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(ServerApp.class.getClassLoader().getResource("server_config.json"), Config.class);
		} catch (Exception e) {
			LOGGER.info("Cannot read config file, using default values", e);
		}

		return new Config();
	}
}
