package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.AbstractCommand;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.command.CommandSender;

public class CommandEpicFurnaces extends AbstractCommand {

    public CommandEpicFurnaces() {
        super("EpicFurnaces", null, false);
    }

    @Override
    protected ReturnType runCommand(EpicFurnaces plugin, CommandSender sender, String... args) {
        sender.sendMessage("");
        plugin.getLocale().newMessage("&7Version " + plugin.getDescription().getVersion()
                + " Created with <3 by &5&l&oSongoda").sendPrefixedMessage(sender);

        for (AbstractCommand command : plugin.getCommandManager().getCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(Methods.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
            }
        }
        sender.sendMessage("");

        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/EpicFurnaces";
    }

    @Override
    public String getDescription() {
        return "Displays this page.";
    }
}
