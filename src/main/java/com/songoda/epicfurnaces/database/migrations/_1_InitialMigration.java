package com.songoda.epicfurnaces.database.migrations;

import com.songoda.core.database.DataMigration;
import com.songoda.core.database.MySQLConnector;
import com.songoda.epicfurnaces.EpicFurnaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {

    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = EpicFurnaces.getInstance().getDatabaseConnector() instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create furnaces table.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "active_furnaces (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
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
            statement.execute("CREATE TABLE " + tablePrefix + "access_list (" +
                    "furnace_id INTEGER NOT NULL, " +
                    "uuid VARCHAR(36)" +
                    ")");
        }

        // Create items to level up.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "to_level_new (" +
                    "furnace_id INTEGER NOT NULL, " +
                    "item TEXT NOT NULL," +
                    "amount INT NOT NULL " +
                    ")");
        }

        // Create player boosts
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "boosted_players (" +
                    "player VARCHAR(36) NOT NULL, " +
                    "multiplier INTEGER NOT NULL," +
                    "end_time BIGINT NOT NULL " +
                    ")");
        }

    }
}
