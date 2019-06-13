package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.AbstractCommand;
import com.songoda.epicfurnaces.furnace.levels.Level;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
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
            sender.sendMessage(plugin.getReferences().getPrefix() + Methods.formatText("&cThat player does not exist or is currently offline."));
            return ReturnType.FAILURE;
        } else if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getReferences().getPrefix() + Methods.formatText("&cYou need to be a player to give a farm item to yourself."));
                return ReturnType.FAILURE;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(args[1]);
        }


        if (args.length >= 3 && !plugin.getLevelManager().isLevel(Integer.parseInt(args[2]))) {
            sender.sendMessage(plugin.getReferences().getPrefix() + Methods.formatText("&cNot a valid level... The current valid levels are: &4" + plugin.getLevelManager().getLowestLevel().getLevel() + "-" + plugin.getLevelManager().getHighestLevel().getLevel() + "&c."));
            return ReturnType.FAILURE;
        } else if (args.length != 1) {

            level = plugin.getLevelManager().getLevel(Integer.parseInt(args[2]));
        }
        player.getInventory().addItem(plugin.createLeveledFurnace(level.getLevel(), 0));
        player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("command.give.success", level.getLevel()));

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
