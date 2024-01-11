package com.craftaro.epicfurnaces.gui;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.gui.CustomizableGui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicfurnaces.settings.Settings;
import com.craftaro.epicfurnaces.EpicFurnaces;
import com.craftaro.epicfurnaces.furnace.Furnace;
import com.craftaro.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIRemoteAccess extends CustomizableGui {
    private final EpicFurnaces plugin;
    private final Furnace furnace;
    private final Player player;

    public GUIRemoteAccess(EpicFurnaces plugin, Furnace furnace, Player player) {
        super(plugin, "remoteAccess");
        this.plugin = plugin;
        this.furnace = furnace;
        this.player = player;

        setRows(6);
        setTitle(Methods.formatName(furnace.getLevel().getLevel()));
        showPage();
    }

    private void showPage() {
        reset();

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill("mirrorfill_1", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_2", 1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill("mirrorfill_3", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_4", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_5", 0, 1, true, true, glass2);

        this.pages = (int) Math.max(1, Math.ceil(this.furnace.getAccessList().size() / ((double) 28)));

        setNextPage(5, 7, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.nametag.next").getMessage()));
        setPrevPage(5, 1, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        setButton("exit", 8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                this.plugin.getLocale().getMessage("general.nametag.exit").getMessage()), (event) -> this.player.closeInventory());

        setButton("addplayer", 4, GuiUtils.createButtonItem(XMaterial.EMERALD,
                this.plugin.getLocale().getMessage("interface.remoteaccess.addplayertitle").getMessage()), (event) -> {
            this.plugin.getLocale().getMessage("event.remote.enterplayer").sendPrefixedMessage(this.player);
            ChatPrompt.showPrompt(this.plugin, this.player, chat -> {
                Player toAdd = Bukkit.getPlayer(chat.getMessage());
                if (toAdd == null) {
                    this.plugin.getLocale().getMessage("event.remote.invalidplayer").sendPrefixedMessage(this.player);
                    return;
                }

                if (this.furnace.getAccessList().contains(toAdd.getUniqueId())) {
                    this.plugin.getLocale().getMessage("event.remote.playeralreadyadded").sendPrefixedMessage(this.player);
                    return;
                }

                this.furnace.addToAccessList(toAdd);
                this.plugin.getDataHelper().createAccessPlayer(this.furnace, toAdd.getUniqueId());
                this.plugin.getLocale().getMessage("event.remote.playeradded").sendPrefixedMessage(this.player);
            }).setOnClose(() -> this.guiManager.showGUI(this.player, new GUIRemoteAccess(this.plugin, this.furnace, this.player)));
        });

        List<UUID> entries = this.furnace.getAccessList().stream().skip((this.page - 1) * 28).limit(28).collect(Collectors.toList());
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            int num = 11;
            for (UUID entry : entries) {
                if (num == 16 || num == 36) {
                    num = num + 2;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry);
                ItemStack itemStack = GuiUtils.createButtonItem(XMaterial.PLAYER_HEAD, TextUtils.formatText("&6" + offlinePlayer.getName()),
                        this.plugin.getLocale().getMessage("interface.remoteaccess.playerinfo")
                                .getMessage().split("\\|"));
                SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
                    meta.setOwningPlayer(offlinePlayer);
                } else {
                    meta.setOwner(offlinePlayer.getName());
                }

                itemStack.setItemMeta(meta);

                setButton(num, itemStack, event -> {
                    this.furnace.removeFromAccessList(entry);
                    this.plugin.getDataHelper().deleteAccessPlayer(this.furnace, entry);
                    this.guiManager.showGUI(this.player, new GUIRemoteAccess(this.plugin, this.furnace, this.player));
                });
                num++;
            }

            update();
        });
    }
}
