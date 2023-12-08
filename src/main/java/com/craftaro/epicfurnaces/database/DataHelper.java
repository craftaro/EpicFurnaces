package com.craftaro.epicfurnaces.database;

import com.craftaro.core.database.Data;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.third_party.org.jooq.impl.DSL;
import com.craftaro.epicfurnaces.EpicFurnaces;
import com.craftaro.epicfurnaces.furnace.Furnace;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class DataHelper {

    private final EpicFurnaces plugin;
    private final Set<Furnace> furnaceUpdateQueue = new HashSet<>();

    public DataHelper(EpicFurnaces plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::bulkUpdateFurnaceQueue, 20 * 60 * 3, 20 * 60 * 3);
    }

    public void bulkUpdateFurnaceQueue() {
        synchronized (this.furnaceUpdateQueue) {
            updateFurnaces(new LinkedHashSet<>(this.furnaceUpdateQueue));
            this.furnaceUpdateQueue.clear();
        }
    }

    public void queueFurnaceForUpdate(Furnace furnace) {
        synchronized (this.furnaceUpdateQueue) {
            this.furnaceUpdateQueue.add(furnace);
        }
    }

    public void dequeueFurnaceForUpdate(Furnace furnace) {
        synchronized (this.furnaceUpdateQueue) {
            this.furnaceUpdateQueue.remove(furnace);
        }
    }

    public void updateFurnaces(Collection<Furnace> furnaces) {
        Set<Data> data = new HashSet<>(furnaces);
        plugin.getDataManager().saveBatch(data);
    }


    public void updateLevelupItems(Furnace furnace, XMaterial material, int amount) {
        String tablePrefix = plugin.getDataManager().getTablePrefix();
        plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
            dslContext.deleteFrom(DSL.table(tablePrefix + "to_level_new"))
                    .where(DSL.field("furnace_id").eq(furnace.getId()))
                    .and(DSL.field("item").eq(material.name()))
                    .execute();

            dslContext.insertInto(DSL.table(tablePrefix + "to_level_new"))
                    .columns(DSL.field("furnace_id"), DSL.field("item"), DSL.field("amount"))
                    .values(furnace.getId(), material.name(), amount)
                    .execute();
        });
    }

    // public void createAccessPlayer(Furnace furnace, UUID uuid) {
    //        this.runAsync(() -> {
    //            try (Connection connection = this.databaseConnector.getConnection()) {
    //                String createAccessPlayer = "INSERT INTO " + this.getTablePrefix() + "access_list (furnace_id, uuid) VALUES (?, ?)";
    //                PreparedStatement statement = connection.prepareStatement(createAccessPlayer);
    //                statement.setInt(1, furnace.getId());
    //                statement.setString(2, uuid.toString());
    //                statement.executeUpdate();
    //            } catch (Exception ex) {
    //                ex.printStackTrace();
    //            }
    //        });
    //    }
    //
    //    // These will be used in the future when the access list gets revamped.
    //    // Probably by me since I already have a custom version in my server.
    //    public void deleteAccessPlayer(Furnace furnace, UUID uuid) {
    //        this.runAsync(() -> {
    //            try (Connection connection = this.databaseConnector.getConnection()) {
    //                String deleteAccessPlayer = "DELETE FROM " + this.getTablePrefix() + "access_list WHERE furnace_id = ? AND uuid = ?";
    //                PreparedStatement statement = connection.prepareStatement(deleteAccessPlayer);
    //                statement.setInt(1, furnace.getId());
    //                statement.setString(2, uuid.toString());
    //                statement.executeUpdate();
    //            } catch (Exception ex) {
    //                ex.printStackTrace();
    //            }
    //        });
    //    }
    //
    //    public void updateAccessPlayers(Furnace furnace) {
    //        this.runAsync(() -> {
    //            try (Connection connection = this.databaseConnector.getConnection()) {
    //                String deletePlayers = "DELETE FROM " + this.getTablePrefix() + "access_list WHERE furnace_id = ?";
    //                try (PreparedStatement statement = connection.prepareStatement(deletePlayers)) {
    //                    statement.setInt(1, furnace.getId());
    //                    statement.executeUpdate();
    //                }
    //
    //                String createAccessPlayer = "INSERT INTO " + this.getTablePrefix() + "access_list (furnace_id, uuid) VALUES (?, ?)";
    //                try (PreparedStatement statement = connection.prepareStatement(createAccessPlayer)) {
    //                    for (UUID uuid : furnace.getAccessList()) {
    //                        statement.setInt(1, furnace.getId());
    //                        statement.setString(2, uuid.toString());
    //                        statement.addBatch();
    //                    }
    //                    statement.executeBatch();
    //                }
    //            } catch (Exception ex) {
    //                ex.printStackTrace();
    //            }
    //        });
    //    }

    public void createAccessPlayer(Furnace furnace, UUID uuid) {
        String tablePrefix = plugin.getDataManager().getTablePrefix();
        plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
            dslContext.insertInto(DSL.table(tablePrefix + "access_list"))
                    .columns(DSL.field("furnace_id"), DSL.field("uuid"))
                    .values(furnace.getId(), uuid.toString())
                    .execute();
        });
    }

    public void deleteAccessPlayer(Furnace furnace, UUID uuid) {
        String tablePrefix = plugin.getDataManager().getTablePrefix();
        plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
            dslContext.deleteFrom(DSL.table(tablePrefix + "access_list"))
                    .where(DSL.field("furnace_id").eq(furnace.getId()))
                    .and(DSL.field("uuid").eq(uuid.toString()))
                    .execute();
        });
    }

    public void updateAccessPlayers(Furnace furnace) {
        String tablePrefix = plugin.getDataManager().getTablePrefix();
        plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
            dslContext.deleteFrom(DSL.table(tablePrefix + "access_list"))
                    .where(DSL.field("furnace_id").eq(furnace.getId()))
                    .execute();

            for (UUID uuid : furnace.getAccessList()) {
                dslContext.insertInto(DSL.table(tablePrefix + "access_list"))
                        .columns(DSL.field("furnace_id"), DSL.field("uuid"))
                        .values(furnace.getId(), uuid.toString())
                        .execute();
            }
        });
    }
}
