package com.unindetec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipInserter implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ShipInserter.class);
    private static final String INSERT_VESSEL_SQL =
            "INSERT INTO embarcacion VALUES(?,?,?,?,?,?,?)";

    private final DatabaseConfig dbConfig;
    private final List<CatSismep> vessels;

    public ShipInserter(List<CatSismep> vessels) {
        this.vessels = vessels;
        this.dbConfig = new DatabaseConfig();

    }

    @Override
    public void run() {
        try (Connection connection = DriverManager.getConnection(
                dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_VESSEL_SQL)) {

            insertVessels(preparedStatement);
            int[] result = preparedStatement.executeBatch();
            logger.info("{} ships inserted successfully", result.length);
        } catch (Exception e) {
            logger.error("Error inserting vessel catalog entries", e);
        }
    }

    private void insertVessels(PreparedStatement preparedStatement) throws SQLException {
        for (CatSismep vessel : vessels) {
            preparedStatement.setString(1, vessel.getId());
            preparedStatement.setString(2, vessel.getMatricula());
            preparedStatement.setString(3, vessel.getNombre());
            preparedStatement.setString(4, vessel.getRazonSocial());
            preparedStatement.setString(5, vessel.getRnp());
            preparedStatement.setString(6, vessel.getCapitan());
            preparedStatement.setFloat(7, vessel.getEslora());
            preparedStatement.addBatch();
        }
    }

}
