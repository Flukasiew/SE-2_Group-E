package com.pw.server.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Data
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private Integer playerConnectionsLimit = 16;

    private Integer gameMasterConnectionTimeout = 5 * 60 * 1000;
    private Integer playerConnectionTimeout = 5 * 60 * 1000;

    private Config() {
    }

    public static Config create() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(new File("server/src/main/resources/config.json").getAbsoluteFile(), Config.class);
        } catch (IOException e) {
            LOGGER.info("Cannot read config file, using default values", e);
        }

        return new Config();
    }
}
