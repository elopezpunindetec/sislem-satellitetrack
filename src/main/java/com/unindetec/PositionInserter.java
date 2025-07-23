package com.unindetec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionInserter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(PositionInserter.class);

    private static final String INSERT_POSITION_SQL =
            "INSERT INTO posiciones (id, latitud, longitud, fecha) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(id) DO UPDATE SET latitud=excluded.latitud, " +
                    "longitud=excluded.longitud, fecha=excluded.fecha";

    private final DatabaseConfig dbConfig;
    private final List<PosSismep> positions;

    public PositionInserter(List<PosSismep> positions) {
        this.positions = positions;
        this.dbConfig = new DatabaseConfig();
    }

    @Override
    public void run() {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_POSITION_SQL)) {

            insertPositions(preparedStatement);
            int[] result = preparedStatement.executeBatch();
            logger.info("{} positions inserted successfully", result.length);
        } catch (Exception e) {
            logger.error("Error inserting positions", e);
        }
    }

    private void insertPositions(PreparedStatement preparedStatement) throws SQLException {
        for (PosSismep position : positions) {
            preparedStatement.setString(1, position.getId());
            preparedStatement.setFloat(2, position.getLatitud());
            preparedStatement.setFloat(3, position.getLongitud());
            preparedStatement.setTimestamp(4, new java.sql.Timestamp(position.getFecha().getTime()));
            preparedStatement.addBatch();
        }
    }
}