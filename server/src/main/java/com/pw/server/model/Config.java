package com.pw.server.model;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Config {
	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

	private Integer portNumber = 1300;
	private String ipAddress = "0.0.0.0";

	private Integer playerConnectionsLimit = 16;

	private Integer gameMasterConnectionTimeout = 5 * 60 * 1000;
	private Integer playerConnectionTimeout = 5 * 60 * 1000;

	private Integer retriesLimit = 5;

	public static Config createFrom(String filename) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			LOGGER.info("Reading config file from {} ...", filename);
			return mapper.readValue(new File(filename), Config.class);
		} catch (Exception e) {
			LOGGER.info("Cannot read config file, using default values..", e);
		}

		return new Config();
	}
}
