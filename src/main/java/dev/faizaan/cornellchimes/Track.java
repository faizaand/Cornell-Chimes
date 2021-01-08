package dev.faizaan.cornellchimes;

import org.bukkit.Material;

/**
 * Represents a single track, defined in config.yml.
 */
public class Track {

    private String id;
    private String friendlyName;
    private Material record;

    public Track(String id, String friendlyName, Material record) {
        this.id = id;
        this.friendlyName = friendlyName;
        this.record = record;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Material getRecord() {
        return record;
    }

    public void setRecord(Material record) {
        this.record = record;
    }
}
