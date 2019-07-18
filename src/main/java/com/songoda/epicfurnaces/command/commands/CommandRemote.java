package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.AbstractCommand;
import com.songoda.epicfurnaces.furnace.Furnace;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandRemote extends AbstractCommand {

    public CommandRemote(AbstractCommand abstractCommand) {
        super("remote", abstractCommand, true);
    }

    @Override
    protected ReturnType runCommand(EpicFurnaces plugin, CommandSender sender, String... args) {

        if (!plugin.getConfig().getBoolean("Main.Access Furnaces Remotely") || !sender.hasPermission("EpicFurnaces.Remote")) {
            plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        if (!plugin.getDataFile().getConfig().contains("data.charged")) {
            return ReturnType.FAILURE;
        }
        if (args.length < 2) return ReturnType.SYNTAX_ERROR;

        StringBuilder name = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            name.append(" ").append(args[i]);
        }
        name = new StringBuilder(name.toString().trim());
        for (Furnace furnace : plugin.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getNickname() == null) continue;

            if (!furnace.getNickname().equalsIgnoreCase(name.toString())) {
                plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
                continue;
            }
            for (UUID uuid : furnace.getAccessList()) {
                if (!uuid.equals(((Player) sender).getUniqueId())) {
                    continue;
                }
                Block b = furnace.getLocation().getBlock();
                org.bukkit.block.Furnace furnaceBlock = (org.bukkit.block.Furnace) b.getState();
                ((Player) sender).openInventory(furnaceBlock.getInventory());
                return ReturnType.SUCCESS;
            }

        }
        plugin.getLocale().getMessage("event.remote.notfound").sendPrefixedMessage(sender);
        return ReturnType.FAILURE;
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/ef remote [nickname]";
    }

    @Override
    public String getDescription() {
        return "Remote control your furnace.";
    }
}
