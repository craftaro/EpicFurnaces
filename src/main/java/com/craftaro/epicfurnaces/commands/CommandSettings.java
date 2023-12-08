package com.craftaro.epicfurnaces.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.configuration.editor.PluginConfigGui;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.epicfurnaces.EpicFurnaces;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {
    private final EpicFurnaces plugin;
    private final GuiManager guiManager;

    public CommandSettings(EpicFurnaces plugin, GuiManager guiManager) {
        super(CommandType.CONSOLE_OK, "settings");
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.guiManager.showGUI((Player) sender, new PluginConfigGui(this.plugin));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.admin.settings";
    }

    @Override
    public String getSyntax() {
        return "settings";
    }

    @Override
    public String getDescription() {
        return "Edit the EpicFurnaces Settings.";
    }
}
