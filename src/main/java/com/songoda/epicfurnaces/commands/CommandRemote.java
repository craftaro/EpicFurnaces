package com.songoda.epicfurnaces.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.settings.Settings;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public class CommandRemote extends AbstractCommand {
    private final EpicFurnaces plugin;

    public CommandRemote(EpicFurnaces plugin) {
        super(CommandType.CONSOLE_OK, "remote");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = ((Player) sender);
        if (!Settings.REMOTE.getBoolean() || !sender.hasPermission("EpicFurnaces.Remote")) {
            this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        if (args.length < 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        String name = String.join(" ", args);
        Furnace furnace = this.plugin.getFurnaceManager().getFurnaces().values()
                .stream().filter(f -> f.getNickname() != null
                        && f.getNickname().equalsIgnoreCase(name)).findFirst().orElse(null);
        if (furnace == null) {
            this.plugin.getLocale().getMessage("event.remote.notfound").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!furnace.isInLoadedChunk()) {
            this.plugin.getLocale().getMessage("event.remote.notloaded").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        for (UUID uuid : furnace.getAccessList()) {
            if (!uuid.equals(((Player) sender).getUniqueId())) {
                continue;
            }
            Block block = furnace.getLocation().getBlock();
            org.bukkit.block.Furnace furnaceBlock = (org.bukkit.block.Furnace) block.getState();
            Inventory inventory = furnaceBlock.getInventory();
            player.openInventory(inventory);
            return ReturnType.SUCCESS;
        }
        this.plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
        return ReturnType.FAILURE;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.remote";
    }

    @Override
    public String getSyntax() {
        return "remote [nickname]";
    }

    @Override
    public String getDescription() {
        return "Remote control your furnace.";
    }
}
