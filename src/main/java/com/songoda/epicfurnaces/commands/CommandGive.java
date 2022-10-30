package com.songoda.epicfurnaces.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.furnace.levels.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class CommandGive extends AbstractCommand implements EpicFurnaceInstances {

    public CommandGive() {
        super(CommandType.CONSOLE_OK, "give");
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length == 1) return ReturnType.SYNTAX_ERROR;

        final EpicFurnaces plugin = getPlugin();
        final Player player;
        if (args.length != 0 && Bukkit.getPlayer(args[0]) == null) {
            plugin.getLocale().newMessage("&cThat player does not exist or is currently offline.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.getLocale().newMessage("&cYou need to be a player to give a farm item to yourself.").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(args[0]);
        }

        Level level = LEVEL_MANAGER.getLowestLevel();
        if (args.length >= 2 && !LEVEL_MANAGER.isLevel(Integer.parseInt(args[1]))) {
            plugin.getLocale().newMessage("&cNot a valid level... The current valid levels are: &4"
                    + LEVEL_MANAGER.getLowestLevel().getLevel() + "-"
                    + LEVEL_MANAGER.getHighestLevel().getLevel() + "&c.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length != 0) {
            level = LEVEL_MANAGER.getLevel(Integer.parseInt(args[1]));
        }
        player.getInventory().addItem(plugin.createLeveledFurnace(Material.FURNACE, level.getLevel(), 0));
        plugin.getLocale().getMessage("command.give.success")
                .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(sender);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender commandSender, String... strings) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.admin.give";
    }

    @Override
    public String getSyntax() {
        return "give [player] <level>";
    }

    @Override
    public String getDescription() {
        return "Give a leveled furnace to a player.";
    }
}
