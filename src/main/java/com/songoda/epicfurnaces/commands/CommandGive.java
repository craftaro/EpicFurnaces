package com.songoda.epicfurnaces.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.levels.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandGive extends AbstractCommand {
    private final EpicFurnaces plugin;

    public CommandGive(EpicFurnaces plugin) {
        super(CommandType.CONSOLE_OK, "give");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length == 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        Level level = this.plugin.getLevelManager().getLowestLevel();
        Player player;
        if (args.length != 0 && Bukkit.getPlayer(args[0]) == null) {
            this.plugin.getLocale().newMessage("&cThat player does not exist or is currently offline.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length == 0) {
            if (!(sender instanceof Player)) {
                this.plugin.getLocale().newMessage("&cYou need to be a player to give a farm item to yourself.").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(args[0]);
        }


        if (args.length >= 2 && !this.plugin.getLevelManager().isLevel(Integer.parseInt(args[1]))) {
            this.plugin.getLocale().newMessage("&cNot a valid level... The current valid levels are: &4"
                    + this.plugin.getLevelManager().getLowestLevel().getLevel() + "-"
                    + this.plugin.getLevelManager().getHighestLevel().getLevel() + "&c.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length != 0) {
            level = this.plugin.getLevelManager().getLevel(Integer.parseInt(args[1]));
        }
        player.getInventory().addItem(this.plugin.createLeveledFurnace(Material.FURNACE, level.getLevel(), 0));
        this.plugin.getLocale().getMessage("command.give.success")
                .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(sender);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicfurnaces.admin.give";
    }

    @Override
    public String getSyntax() {
        return "give [player] <level>";
    }

    @Override
    public String getDescription() {
        return "Give a leveled furnace to a player.";
    }
}
