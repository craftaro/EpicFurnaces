package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.AbstractCommand;
import com.songoda.epicfurnaces.objects.Level;
import com.songoda.epicfurnaces.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGive extends AbstractCommand {
    private final EpicFurnaces instance;

    public CommandGive(EpicFurnaces instance, AbstractCommand parent) {
        super("give", parent, false);
        this.instance = instance;

    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length == 2) {
            return ReturnType.SYNTAX_ERROR;
        }

        Level level = instance.getLevelManager().getLowestLevel();
        Player player;
        if (args.length != 1 && Bukkit.getPlayer(args[1]) == null) {
            sender.sendMessage(instance.getLocale().getPrefix() + StringUtils.formatText("&cThat player does not exist or is currently offline."));
            return ReturnType.FAILURE;
        } else if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(instance.getLocale().getPrefix() + StringUtils.formatText("&cYou need to be a player to give a farm item to yourself."));
                return ReturnType.FAILURE;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(args[1]);
        }


        if (args.length >= 3 && !instance.getLevelManager().isLevel(Integer.parseInt(args[2]))) {
            sender.sendMessage(instance.getLocale().getPrefix() + StringUtils.formatText("&cNot a valid level... The current valid levels are: &4" + instance.getLevelManager().getLowestLevel().getLevel() + "-" + instance.getLevelManager().getHighestLevel().getLevel() + "&c."));
            return ReturnType.FAILURE;
        } else if (args.length != 1) {

            level = instance.getLevelManager().getLevel(Integer.parseInt(args[2]));
        }
        player.getInventory().addItem(instance.getFurnaceManager().createLeveledFurnace(level.getLevel(), 0, instance));
        player.sendMessage(instance.getLocale().getPrefix() + instance.getLocale().getMessage("command.give.success", level.getLevel()));

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
        return "Give a leveled objects to a player.";
    }
}
