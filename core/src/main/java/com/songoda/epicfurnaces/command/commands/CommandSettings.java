package com.songoda.epicfurnaces.command.commands;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSettings extends AbstractCommand {
    private final EpicFurnaces instance;

    public CommandSettings(EpicFurnaces instance, AbstractCommand parent) {
        super("settings", parent, true);
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        instance.getSettingsManager().openSettingsManager((Player) sender);
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.admin";
    }

    @Override
    public String getSyntax() {
        return "/ef settings";
    }

    @Override
    public String getDescription() {
        return "Edit the EpicFurnaces Settings.";
    }
}
