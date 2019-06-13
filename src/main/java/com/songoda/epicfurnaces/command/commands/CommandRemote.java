package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnacesPlugin;
import com.songoda.epicfurnaces.api.furnace.Furnace;
import com.songoda.epicfurnaces.command.AbstractCommand;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandRemote extends AbstractCommand {

    public CommandRemote(AbstractCommand abstractCommand) {
        super("remote", abstractCommand, true);
    }

    @Override
    protected ReturnType runCommand(EpicFurnacesPlugin instance, CommandSender sender, String... args) {

        if (!instance.getConfig().getBoolean("Main.Access Furnaces Remotely") || !sender.hasPermission("EpicFurnaces.Remote")) {
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
            return ReturnType.FAILURE;
        }
        if (!instance.getDataFile().getConfig().contains("data.charged")) {
            return ReturnType.FAILURE;
        }
        if (args.length < 2) return ReturnType.SYNTAX_ERROR;

        StringBuilder name = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            name.append(" ").append(args[i]);
        }
        name = new StringBuilder(name.toString().trim());
        for (Furnace furnace : instance.getFurnaceManager().getFurnaces().values()) {
            if (furnace.getNickname() == null) continue;

            if (!furnace.getNickname().equalsIgnoreCase(name.toString())) {
                sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
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
        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.remote.notfound"));
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
