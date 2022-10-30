package com.songoda.epicfurnaces.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CommandBoost extends AbstractCommand implements EpicFurnaceInstances {

    public CommandBoost() {
        super(CommandType.CONSOLE_OK, "boost");
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        final EpicFurnaces instance = getPlugin();
        if (args.length < 2) {
            instance.getLocale().newMessage("&7Syntax error...").sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }
        if (!Methods.isInt(args[1])) {
            instance.getLocale().newMessage("&6" + args[1] + " &7is not a number...").sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long duration = 0L;
        if (args.length > 2) {
            for (int i = 0; i < args.length; i++) {
                String line = args[i];
                long time = Methods.parseTime(line);
                duration += time;
            }
        }

        final Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            instance.getLocale().newMessage("&cThat player does not exist or is not online...").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        final BoostData boostData = new BoostData(Integer.parseInt(args[1]), duration == 0L ? Long.MAX_VALUE : System.currentTimeMillis() + duration, player.getUniqueId());
        BOOST_MANAGER.addBoostToPlayer(boostData);
        instance.getDataManager().createBoost(boostData);
        instance.getLocale().newMessage("&7Successfully boosted &6" + Bukkit.getPlayer(args[0]).getName()
                + "'s &7furnace reward amounts &6" + args[1] + "x" + (duration == 0L ? "" : (" for " + Methods.makeReadable(duration))) + "&7.").sendPrefixedMessage(sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        } else if (args.length == 2) {
            return Arrays.asList("1", "2", "3", "4", "5");
        } else if (args.length == 3) {
            return Arrays.asList("1m", "1h", "1d");
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.admin.boost";
    }

    @Override
    public String getSyntax() {
        return "boost <player> <amount> [duration]";
    }

    @Override
    public String getDescription() {
        return "This allows you to boost a players furnace reward amounts by a multiplier (Put 2 for double, 3 for triple and so on).";
    }
}
