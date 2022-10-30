package com.songoda.epicfurnaces.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.locale.Locale;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicfurnaces.EpicFurnaceInstances;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.furnace.Furnace;
import com.songoda.epicfurnaces.settings.Settings;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class GUIRemoteAccess extends CustomizableGui implements EpicFurnaceInstances {

    private final Furnace furnace;
    private final Player player;

    public GUIRemoteAccess(Furnace furnace, Player player) {
        super(EpicFurnaces.getInstance(), "remoteAccess");
        this.furnace = furnace;
        this.player = player;

        setRows(6);
        setTitle(Methods.formatName(furnace.getLevel().getLevel()));
        showPage();
    }

    private void showPage() {
        reset();

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill("mirrorfill_1", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_2", 1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill("mirrorfill_3", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_4", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_5", 0, 1, true, true, glass2);

        pages = (int) Math.max(1, Math.ceil(furnace.getAccessList().size() / ((double) 28)));
        
        final EpicFurnaces plugin = getPlugin();
        final Locale locale = plugin.getLocale();
        setNextPage(5, 7, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, locale.getMessage("general.nametag.next").getMessage()));
        setPrevPage(5, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, locale.getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        setButton("exit", 8, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                locale.getMessage("general.nametag.exit").getMessage()), (event) -> player.closeInventory());

        setButton("addplayer", 4, GuiUtils.createButtonItem(CompatibleMaterial.EMERALD,
                locale.getMessage("interface.remoteaccess.addplayertitle").getMessage()), (event) -> {
            locale.getMessage("event.remote.enterplayer").sendPrefixedMessage(player);
            ChatPrompt.showPrompt(plugin, player, chat -> {
                Player toAdd = Bukkit.getPlayer(chat.getMessage());
                if (toAdd == null) {
                    locale.getMessage("event.remote.invalidplayer").sendPrefixedMessage(player);
                    return;
                }

                if (furnace.getAccessList().contains(toAdd.getUniqueId())) {
                    locale.getMessage("event.remote.playeralreadyadded").sendPrefixedMessage(player);
                    return;
                }

                furnace.addToAccessList(toAdd);
                plugin.getDataManager().createAccessPlayer(furnace, toAdd.getUniqueId());
                locale.getMessage("event.remote.playeradded").sendPrefixedMessage(player);
            }).setOnClose(() -> guiManager.showGUI(player, new GUIRemoteAccess(furnace, player)));
        });

        final List<UUID> entries = furnace.getAccessList().stream().skip((page - 1) * 28).limit(28).collect(Collectors.toList());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int num = 11;
            for (UUID entry : entries) {
                if (num == 16 || num == 36)
                    num = num + 2;
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry);
                final ItemStack itemStack = GuiUtils.createButtonItem(CompatibleMaterial.PLAYER_HEAD, TextUtils.formatText("&6" + offlinePlayer.getName()),
                        locale.getMessage("interface.remoteaccess.playerinfo")
                                .getMessage().split("\\|"));
                SkullMeta meta = (SkullMeta)itemStack.getItemMeta();

                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
                    meta.setOwningPlayer(offlinePlayer);
                else
                    meta.setOwner(offlinePlayer.getName());

                itemStack.setItemMeta(meta);

                setButton(num, itemStack, event -> {
                    furnace.removeFromAccessList(entry);
                    plugin.getDataManager().deleteAccessPlayer(furnace, entry);
                    guiManager.showGUI(player, new GUIRemoteAccess(furnace, player));
                });
                num++;
            }

            update();
        });
    }
}
