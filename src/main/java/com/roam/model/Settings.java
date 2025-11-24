package com.roam.model;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    private boolean isLockEnabled = false;
    private String pinHash = "";
    private String theme = "light";
    private List<String> regions = new ArrayList<>();

    public Settings() {
        // Default regions - matching database regions
        regions.add("Lifestyle");
        regions.add("Knowledge");
        regions.add("Skill");
        regions.add("Spirituality");
        regions.add("Career");
        regions.add("Finance");
        regions.add("Social");
        regions.add("Academic");
        regions.add("Relationship");
    }

    public boolean isLockEnabled() {
        return isLockEnabled;
    }

    public void setLockEnabled(boolean lockEnabled) {
        isLockEnabled = lockEnabled;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<String> getRegions() {
        return regions;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
    }
}
