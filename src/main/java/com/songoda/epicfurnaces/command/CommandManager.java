package com.songoda.epicfurnaces.command;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.commands.*;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private EpicFurnaces plugin;

    private List<AbstractCommand> commands = new ArrayList<>();

    public CommandManager(EpicFurnaces plugin) {
        this.plugin = plugin;

        plugin.getCommand("EpicFurnaces").setExecutor(this);

        AbstractCommand commandEpicFurnaces = addCommand(new CommandEpicFurnaces());

        addCommand(new CommandReload(commandEpicFurnaces));
        addCommand(new CommandRemote(commandEpicFurnaces));
        addCommand(new CommandSettings(commandEpicFurnaces));
        addCommand(new CommandGive(commandEpicFurnaces));
        addCommand(new CommandBoost(commandEpicFurnaces));
    }

    private AbstractCommand addCommand(AbstractCommand abstractCommand) {
        commands.add(abstractCommand);
        return abstractCommand;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getCommand().equalsIgnoreCase(command.getName())) {
                if (strings.length == 0) {
                    processRequirements(abstractCommand, commandSender, strings);
                    return true;
                }
            } else if (strings.length != 0 && abstractCommand.getParent() != null && abstractCommand.getParent().getCommand().equalsIgnoreCase(command.getName())) {
                String cmd = strings[0];
                if (cmd.equalsIgnoreCase(abstractCommand.getCommand())) {
                    processRequirements(abstractCommand, commandSender, strings);
                    return true;
                }
            }
        }
        commandSender.sendMessage(plugin.getReferences().getPrefix() + Methods.formatText("&7The command you entered does not exist or is spelt incorrectly."));
        return true;
    }

    private void processRequirements(AbstractCommand command, CommandSender sender, String[] strings) {
        if (!(sender instanceof Player) && command.isNoConsole()) {
            sender.sendMessage("You must be a player to use this command.");
            return;
        }
        if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
            AbstractCommand.ReturnType returnType = command.runCommand(plugin, sender, strings);
            if (returnType == AbstractCommand.ReturnType.SYNTAX_ERROR) {
                sender.sendMessage(plugin.getReferences().getPrefix() + Methods.formatText("&cInvalid Syntax!"));
                sender.sendMessage(plugin.getReferences().getPrefix() + Methods.formatText("&7The valid syntax is: &6" + command.getSyntax() + "&7."));
            }
            return;
        }
        sender.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.general.nopermission"));
    }

    public List<AbstractCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
