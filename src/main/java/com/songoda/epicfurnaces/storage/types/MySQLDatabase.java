package com.songoda.epicfurnaces.storage.types;

import com.songoda.epicfurnaces.EpicFurnaces;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase {

    private final EpicFurnaces plugin;

    private Connection connection;

    public MySQLDatabase(EpicFurnaces plugin) {
        this.plugin = plugin;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + plugin.getConfig().getString("Database.IP") + ":" + plugin.getConfig().getString("Database.Port") + "/" + plugin.getConfig().getString("Database.Database Name") + "?autoReconnect=true&useSSL=false";
            this.connection = DriverManager.getConnection(url, plugin.getConfig().getString("Database.Username"), plugin.getConfig().getString("Database.Password"));

            //ToDo: This is sloppy
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `" + plugin.getConfig().getString("Database.Prefix") + "charged` (\n" +
                    "\t`location` TEXT NULL,\n" +
                    "\t`level` INT NULL,\n" +
                    "\t`uses` INT NULL,\n" +
                    "\t`tolevel` INT NULL,\n" +
                    "\t`nickname` TEXT NULL,\n" +
                    "\t`accesslist` TEXT NULL,\n" +
                    "\t`placedby` TEXT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + plugin.getConfig().getString("Database.Prefix") + "boosts` (\n" +
                    "\t`endtime` TEXT NULL,\n" +
                    "\t`amount` INT NULL,\n" +
                    "\t`uuid` TEXT NULL\n" +
                    ")");

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed.");
        }
    }

    public Connection getConnection() {
        return connection;
    }
}