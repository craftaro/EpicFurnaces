package com.songoda.epicfurnaces.utils.gui;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.utils.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AbstractAnvilGUI {

    private static Class<?> BlockPosition;
    private static Class<?> PacketPlayOutOpenWindow;
    private static Class<?> ContainerAnvil;
    private static Class<?> ChatMessage;
    private static Class<?> EntityHuman;
    private static boolean loadedClasses = false;
    private Player player;
    @SuppressWarnings("unused")
    private AnvilClickEventHandler handler;
    private Map<AnvilSlot, ItemStack> items = new HashMap<>();
    private OnClose onClose = null;
    private Inventory inv;
    private Listener listener;

    public AbstractAnvilGUI(final Player player, final AnvilClickEventHandler handler) {
        loadClasses();
        this.player = player;
        this.handler = handler;

        this.listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (!(event.getWhoClicked() instanceof Player) || !event.getInventory().equals(inv)) return;

                event.setCancelled(true);

                ItemStack item = event.getCurrentItem();
                int slot = event.getRawSlot();
                String name = "";

                if (item != null) {
                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();

                        if (meta.hasDisplayName()) {
                            name = meta.getDisplayName();
                        }
                    }
                }

                AnvilClickEvent clickEvent = new AnvilClickEvent(AnvilSlot.bySlot(slot), name);

                handler.onAnvilClick(clickEvent);

                if (clickEvent.getWillClose()) {
                    event.getWhoClicked().closeInventory();
                }

                if (clickEvent.getWillDestroy()) {
                    destroy();
                }
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (!(event.getPlayer() instanceof Player)) return;
                Inventory inv = event.getInventory();
                player.setLevel(player.getLevel() - 1);
                if (!inv.equals(AbstractAnvilGUI.this.inv)) return;
                inv.clear();
                OnClose onClose = getOnClose();
                Bukkit.getScheduler().scheduleSyncDelayedTask(EpicFurnaces.getInstance(), () -> {
                    if (onClose != null) onClose.OnClose(player, inv);
                    destroy();
                }, 1L);
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                if (!event.getPlayer().equals(getPlayer())) return;
                player.setLevel(player.getLevel() - 1);
                destroy();
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, EpicFurnaces.getInstance());
    }

    private void loadClasses() {
        if (loadedClasses) return;
        BlockPosition = NMSUtil.getNMSClass("BlockPosition");
        PacketPlayOutOpenWindow = NMSUtil.getNMSClass("PacketPlayOutOpenWindow");
        ContainerAnvil = NMSUtil.getNMSClass("ContainerAnvil");
        EntityHuman = NMSUtil.getNMSClass("EntityHuman");
        ChatMessage = NMSUtil.getNMSClass("ChatMessage");
        loadedClasses = true;
    }

    public Player getPlayer() {
        return player;
    }

    public void setSlot(AnvilSlot slot, ItemStack item) {
        items.put(slot, item);
    }

    public void open() {
        player.setLevel(player.getLevel() + 1);

        try {
            Object craftPlayer = Objects.requireNonNull(NMSUtil.getCraftClass("entity.CraftPlayer")).cast(player);
            Method getHandleMethod = craftPlayer.getClass().getMethod("getHandle");
            Object entityPlayer = getHandleMethod.invoke(craftPlayer);

            Object container;

            if (NMSUtil.getVersionNumber() == 7) {
                container = ContainerAnvil.getConstructor(new Class[]{NMSUtil.getNMSClass("PlayerInventory"), NMSUtil.getNMSClass("World"), Integer.TYPE, Integer.TYPE, Integer.TYPE, EntityHuman}).newInstance(NMSUtil.getFieldObject(entityPlayer, NMSUtil.getField(entityPlayer.getClass(), "inventory", false)), NMSUtil.getFieldObject(entityPlayer, NMSUtil.getField(entityPlayer.getClass(), "world", false)), 0, 0, 0, entityPlayer);
            } else {
                container = ContainerAnvil.getConstructor(NMSUtil.getNMSClass("PlayerInventory"), NMSUtil.getNMSClass("World"), BlockPosition, EntityHuman).newInstance(NMSUtil.getFieldObject(entityPlayer, NMSUtil.getField(entityPlayer.getClass(), "inventory", false)), NMSUtil.getFieldObject(entityPlayer, NMSUtil.getField(entityPlayer.getClass(), "world", false)), BlockPosition.getConstructor(int.class, int.class, int.class).newInstance(0, 0, 0), entityPlayer);
            }

            Objects.requireNonNull(NMSUtil.getField(NMSUtil.getNMSClass("Container"), "checkReachable", true)).set(container, false);

            Method getBukkitViewMethod = container.getClass().getMethod("getBukkitView");
            Object bukkitView = getBukkitViewMethod.invoke(container);
            Method getTopInventoryMethod = bukkitView.getClass().getMethod("getTopInventory");
            inv = (Inventory) getTopInventoryMethod.invoke(bukkitView);

            for (AnvilSlot slot : items.keySet()) {
                inv.setItem(slot.getSlot(), items.get(slot));
            }

            Method nextContainerCounterMethod = entityPlayer.getClass().getMethod("nextContainerCounter");
            int c = (int) nextContainerCounterMethod.invoke(entityPlayer);

            Constructor<?> chatMessageConstructor = ChatMessage.getConstructor(String.class, Object[].class);
            Object packet;

            if (NMSUtil.getVersionNumber() == 7) {
                packet = PacketPlayOutOpenWindow.getConstructor(new Class[]{Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE, Boolean.TYPE, Integer.TYPE}).newInstance(c, 8, "Repairing", 0, Boolean.TRUE, 0);
            } else {
                packet = PacketPlayOutOpenWindow.getConstructor(int.class, String.class, NMSUtil.getNMSClass("IChatBaseComponent"), int.class).newInstance(c, "minecraft:anvil", chatMessageConstructor.newInstance("Repairing", new Object[]{}), 0);
            }

            NMSUtil.sendPacket(player, packet);

            Field activeContainerField = NMSUtil.getField(EntityHuman, "activeContainer", true);

            if (activeContainerField != null) {
                activeContainerField.set(entityPlayer, container);
                Objects.requireNonNull(NMSUtil.getField(NMSUtil.getNMSClass("Container"), "windowId", true)).set(activeContainerField.get(entityPlayer), c);

                Method addSlotListenerMethod = activeContainerField.get(entityPlayer).getClass().getMethod("addSlotListener", NMSUtil.getNMSClass("ICrafting"));
                addSlotListenerMethod.invoke(activeContainerField.get(entityPlayer), entityPlayer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        onClose = null;
        player = null;
        handler = null;
        items = null;

        HandlerList.unregisterAll(listener);

        listener = null;
    }

    private OnClose getOnClose() {
        return onClose;
    }

    public void setOnClose(OnClose onClose) {
        this.onClose = onClose;
    }

    public enum AnvilSlot {
        INPUT_LEFT(0),
        INPUT_RIGHT(1),
        OUTPUT(2);

        private int slot;

        AnvilSlot(int slot) {
            this.slot = slot;
        }

        public static AnvilSlot bySlot(int slot) {
            for (AnvilSlot anvilSlot : values()) {
                if (anvilSlot.getSlot() == slot) {
                    return anvilSlot;
                }
            }

            return null;
        }

        public int getSlot() {
            return slot;
        }
    }

    public interface AnvilClickEventHandler {
        void onAnvilClick(AnvilClickEvent event);
    }

    public class AnvilClickEvent {
        private AnvilSlot slot;

        private String name;

        private boolean close = true;
        private boolean destroy = true;

        public AnvilClickEvent(AnvilSlot slot, String name) {
            this.slot = slot;
            this.name = name;
        }

        public AnvilSlot getSlot() {
            return slot;
        }

        public String getName() {
            return name;
        }

        public boolean getWillClose() {
            return close;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public boolean getWillDestroy() {
            return destroy;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }
    }
}

