package com.craftaro.epicfurnaces.database;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicfurnaces.furnace.FurnaceBuilder;
import com.craftaro.epicfurnaces.EpicFurnaces;
import com.craftaro.epicfurnaces.boost.BoostData;
import com.craftaro.epicfurnaces.furnace.Furnace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class DataManager {


    public DataManager(DatabaseConnector connector, Plugin plugin) {
        //super(connector, plugin);

        // Updating furnaces every 3 minutes should be plenty I believe

    }




//    public void getFurnaces(Consumer<Map<Integer, Furnace>> callback) {
//        this.runAsync(() -> {
//            try (Connection connection = this.databaseConnector.getConnection()) {
//                Map<Integer, Furnace> furnaces = new HashMap<>();
//
//                try (Statement statement = connection.createStatement()) {
//                    String selectFurnaces = "SELECT * FROM " + this.getTablePrefix() + "active_furnaces";
//                    ResultSet result = statement.executeQuery(selectFurnaces);
//                    while (result.next()) {
//                        World world = Bukkit.getWorld(result.getString("world"));
//
//                        if (world == null) {
//                            continue;
//                        }
//
//                        int id = result.getInt("id");
//                        int level = result.getInt("level");
//                        int uses = result.getInt("uses");
//
//                        String placedByStr = result.getString("placed_by");
//                        UUID placedBy = placedByStr == null ? null : UUID.fromString(result.getString("placed_by"));
//
//                        String nickname = result.getString("nickname");
//
//                        int x = result.getInt("x");
//                        int y = result.getInt("y");
//                        int z = result.getInt("z");
//                        Location location = new Location(world, x, y, z);
//
//                        Furnace furnace = new FurnaceBuilder(location)
//                                .setId(id)
//                                .setLevel(EpicFurnaces.getInstance().getLevelManager().getLevel(level))
//                                .setUses(uses)
//                                .setPlacedBy(placedBy)
//                                .setNickname(nickname)
//                                .build();
//
//                        furnaces.put(id, furnace);
//                    }
//                }
//
//                try (Statement statement = connection.createStatement()) {
//                    String selectAccessList = "SELECT * FROM " + this.getTablePrefix() + "access_list";
//                    ResultSet result = statement.executeQuery(selectAccessList);
//                    while (result.next()) {
//                        int id = result.getInt("furnace_id");
//                        UUID uuid = UUID.fromString(result.getString("uuid"));
//
//                        Furnace furnace = furnaces.get(id);
//                        if (furnace == null) {
//                            break;
//                        }
//
//                        furnace.addToAccessList(uuid);
//                    }
//                }
//
//                try (Statement statement = connection.createStatement()) {
//                    String selectLevelupItems = "SELECT * FROM " + this.getTablePrefix() + "to_level_new";
//                    ResultSet result = statement.executeQuery(selectLevelupItems);
//                    while (result.next()) {
//                        int id = result.getInt("furnace_id");
//                        XMaterial material = CompatibleMaterial.getMaterial(result.getString("item")).get();
//                        int amount = result.getInt("amount");
//
//                        Furnace furnace = furnaces.get(id);
//                        if (furnace == null) {
//                            break;
//                        }
//
//                        furnace.addToLevel(material, amount);
//                    }
//                }
//                this.sync(() -> callback.accept(furnaces));
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//    }
}
