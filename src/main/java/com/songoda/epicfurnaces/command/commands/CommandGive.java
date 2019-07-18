package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.AbstractCommand;
import com.songoda.epicfurnaces.furnace.levels.Level;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGive extends AbstractCommand {

    public CommandGive(AbstractCommand parent) {
        super("give", parent, false);
    }

    @Override
    protected ReturnType runCommand(EpicFurnaces plugin, CommandSender sender, String... args) {
        if (args.length == 2) return ReturnType.SYNTAX_ERROR;

        Level level = plugin.getLevelManager().getLowestLevel();
        Player player;
        if (args.length != 1 && Bukkit.getPlayer(args[1]) == null) {
            plugin.getLocale().newMessage("&cThat player does not exist or is currently offline.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length == 1) {
            if (!(sender instanceof Player)) {
                plugin.getLocale().newMessage("&cYou need to be a player to give a farm item to yourself.").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(args[1]);
        }


        if (args.length >= 3 && !plugin.getLevelManager().isLevel(Integer.parseInt(args[2]))) {
            plugin.getLocale().newMessage("&cNot a valid level... The current valid levels are: &4"
                    + plugin.getLevelManager().getLowestLevel().getLevel() + "-"
                    + plugin.getLevelManager().getHighestLevel().getLevel() + "&c.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length != 1) {

            level = plugin.getLevelManager().getLevel(Integer.parseInt(args[2]));
        }
        player.getInventory().addItem(plugin.createLeveledFurnace(Material.FURNACE, level.getLevel(), 0));
        plugin.getLocale().getMessage("command.give.success")
                .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(sender);

        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.admin";
    }

    @Override
    public String getSyntax() {
        return "/ef give [player] <level>";
    }

    @Override
    public String getDescription() {
        return "Give a leveled furnace to a player.";
    }
}
