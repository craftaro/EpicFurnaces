package com.craftaro.epicfurnaces.database.migrations;

import com.craftaro.core.database.DataMigration;
import com.craftaro.core.database.MySQLConnector;
import com.craftaro.epicfurnaces.EpicFurnaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {

    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {

        // Create furnaces table.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "active_furnaces (" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "level INTEGER NOT NULL, " +
                    "uses INTEGER NOT NULL," +
                    "placed_by VARCHAR(36), " +
                    "nickname TEXT, " +
                    "world TEXT NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL " +
                    ")");
        }

        // Create access lists.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "access_list (" +
                    "furnace_id INTEGER NOT NULL, " +
                    "uuid VARCHAR(36)" +
                    ")");
        }

        // Create items to level up.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "to_level_new (" +
                    "furnace_id INTEGER NOT NULL, " +
                    "item TEXT NOT NULL," +
                    "amount INT NOT NULL " +
                    ")");
        }

        // Create player boosts
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "boosted_players (" +
                    "player VARCHAR(36) NOT NULL, " +
                    "multiplier INTEGER NOT NULL," +
                    "end_time BIGINT NOT NULL " +
                    ")");
        }
    }
}
