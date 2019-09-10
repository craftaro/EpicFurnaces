package com.songoda.epicfurnaces.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.settings.Settings;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CommandRemote extends AbstractCommand {

    final EpicFurnaces plugin;

    public CommandRemote(EpicFurnaces plugin) {
        super(true, "remote");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {

        if (!Settings.REMOTE.getBoolean() || !sender.hasPermission("EpicFurnaces.Remote")) {
            plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        if (args.length < 1) return ReturnType.SYNTAX_ERROR;

        StringBuilder name = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
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
    protected List<String> onTab(CommandSender commandSender, String... strings) {
        return null;
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
