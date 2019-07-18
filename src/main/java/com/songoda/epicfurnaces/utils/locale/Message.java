package com.songoda.epicfurnaces.utils.locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message {

    private String prefix = null;
    private String message;

    Message(String message) {
        this.message = message;
    }

    public void sendMessage(Player player) {
        player.sendMessage(this.getMessage());
    }

    public void sendPrefixedMessage(Player player) {
        player.sendMessage(this.getPrefixedMessage());
    }

    public void sendMessage(CommandSender sender) {
        sender.sendMessage(this.getMessage());
    }

    public void sendPrefixedMessage(CommandSender sender) {
        sender.sendMessage(this.getPrefixedMessage());
    }

    public String getPrefixedMessage() {
        return ChatColor.translateAlternateColorCodes('&',(prefix == null ? "" : this.prefix)
                + " " +  this.message);
    }

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes('&', this.message);
    }

    public String getUnformattedMessage() {
        return this.message;
    }

    public Message processPlaceholder(String placeholder, Object replacement) {
        this.message = message.replace("%" + placeholder + "%", replacement.toString());
        return this;
    }

    Message setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toString() {
        return this.message;
    }
}