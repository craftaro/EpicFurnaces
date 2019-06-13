package com.songoda.ultimatekits.hologram;

import com.songoda.ultimatekits.UltimateKits;
import com.songoda.ultimatekits.kit.Kit;
import com.songoda.ultimatekits.kit.KitBlockData;
import com.songoda.ultimatekits.kit.KitType;
import com.songoda.ultimatekits.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Hologram {

    protected final UltimateKits instance;

    Hologram(UltimateKits instance) {
        this.instance = instance;
    }

    public void loadHolograms() {
        Collection<KitBlockData> kitBlocks = instance.getKitManager().getKitLocations().values();
        if (kitBlocks.size() == 0) return;

        for (KitBlockData data : kitBlocks) {
            if (data.getWorld() == null) continue;
                add(data);
        }
    }

    public void unloadHolograms() {
        Collection<KitBlockData> kitBlocks = instance.getKitManager().getKitLocations().values();
        if (kitBlocks.size() == 0) return;

        for (KitBlockData data : kitBlocks) {
            if (data.getWorld() == null) continue;
            remove(data);
        }
    }

    public void add(KitBlockData data) {
        format(data, Action.ADD);
    }

    public void remove(KitBlockData data) {
        format(data, Action.REMOVE);
    }

    public void remove(Kit kit) {
        for (KitBlockData data : instance.getKitManager().getKitLocations().values()) {
            if (data.getKit() != kit) continue;
            remove(data);
        }
    }

    public void update(KitBlockData data) {
        format(data, Action.UPDATE);
    }

    public void update(Kit kit) {
        for (KitBlockData data : instance.getKitManager().getKitLocations().values()) {
            if (data.getKit() != kit) continue;
            update(data);
        }
    }

    private void format(KitBlockData data, Action action) {
        if (data == null) return;
        KitType kitType = data.getType();

        ArrayList<String> lines = new ArrayList<>();

        List<String> order = instance.getConfig().getStringList("Main.Hologram Layout");

        Kit kit = data.getKit();

        for (String o : order) {
            switch (o.toUpperCase()) {
                case "{TITLE}":
                    String title = kit.getTitle();
                    if (title == null) {
                        lines.add(Methods.formatText("&5" + Methods.formatText(kit.getName(), true)));
                    } else {
                        lines.add(Methods.formatText("&5" + Methods.formatText(title)));
                    }
                    break;
                case "{RIGHT-CLICK}":
                    if (kitType == KitType.CRATE) {
                        lines.add(Methods.formatText(instance.getLocale().getMessage("interface.hologram.crate")));
                        break;
                    }
                    if (kit.getLink() != null) {
                        lines.add(Methods.formatText(instance.getLocale().getMessage("interface.hologram.buylink")));
                        break;
                    }
                    if (kit.getPrice() != 0) {
                        lines.add(Methods.formatText(instance.getLocale().getMessage("interface.hologram.buyeco", kit.getPrice() != 0 ? Methods.formatEconomy(kit.getPrice()) : instance.getLocale().getMessage("general.type.free"))));
                    }
                    break;
                case "{LEFT-CLICK}":
                    if (kitType == KitType.CLAIM) {
                        lines.add(Methods.formatText(instance.getLocale().getMessage("interface.hologram.daily")));
                        break;
                    }
                    if (kit.getLink() == null && kit.getPrice() == 0) {
                        lines.add(Methods.formatText(instance.getLocale().getMessage("interface.hologram.previewonly")));
                    } else {
                        lines.add(Methods.formatText(instance.getLocale().getMessage("interface.hologram.preview")));
                    }
                    break;
                default:
                    lines.add(Methods.formatText(o));
                    break;

            }
        }

        double multi = .25 * lines.size();
        Location location = data.getLocation();
        Block b = location.getBlock();

        if (data.isDisplayingItems()) multi += .40;

        if (b.getType() == Material.TRAPPED_CHEST
                || b.getType() == Material.CHEST
                || b.getType().name().contains("SIGN")
                || b.getType() == Material.ENDER_CHEST) multi -= .15;

        location.add(0, multi, 0);

        if (!data.showHologram()) {
            remove(location);
            return;
        }

        switch (action) {
            case UPDATE:
                update(location, lines);
                break;
            case ADD:
                add(location, lines);
                break;
            case REMOVE:
                remove(location);
                break;
        }
    }

    protected abstract void add(Location location, ArrayList<String> lines);

    protected abstract void remove(Location location);

    protected abstract void update(Location location, ArrayList<String> lines);

    public enum Action {

        UPDATE, ADD, REMOVE

    }

}
