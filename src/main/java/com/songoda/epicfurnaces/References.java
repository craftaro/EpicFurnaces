package com.songoda.epicfurnaces;

public class References {

    private String prefix;

    public References() {
        prefix = EpicFurnaces.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}
