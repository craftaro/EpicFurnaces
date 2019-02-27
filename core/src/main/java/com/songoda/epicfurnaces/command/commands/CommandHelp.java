package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.AbstractCommand;
import com.songoda.epicfurnaces.utils.StringUtils;
import org.bukkit.command.CommandSender;

public class CommandHelp extends AbstractCommand {
    private final EpicFurnaces instance;

    public CommandHelp(EpicFurnaces instance, AbstractCommand parent) {
        super("help", parent, false);
        this.instance = instance;

    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        sender.sendMessage("");
        sender.sendMessage(StringUtils.formatText(instance.getLocale().getPrefix() + "&7Version " + instance.getDescription().getVersion() + " Created with <3 by &5&l&oSongoda"));

        for (AbstractCommand command : instance.getCommandManager().getCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(StringUtils.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
            }
        }
        sender.sendMessage("");

        return ReturnType.SUCCESS;
    }

    @Override
    public String getDescription() {
        return "Displays the help page.";
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/ef help";
    }
}
