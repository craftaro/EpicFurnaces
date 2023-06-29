package com.songoda.epicfurnaces.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {
    private final EpicFurnaces plugin;

    public CommandReload(EpicFurnaces plugin) {
        super(CommandType.CONSOLE_OK, "reload");
        this.plugin = plugin;
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        this.plugin.reloadConfig();
        this.plugin.getLocale().getMessage("&7Configuration and Language files reloaded.").sendPrefixedMessage(sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.admin.reload";
    }

    @Override
    public String getSyntax() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}
