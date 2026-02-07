package com.skyblock.upgrade;

public class IslandUpgradeData {
    private int borderLevel;
    private int memberLevel;
    private int pistonLevel;

    public IslandUpgradeData(int borderLevel, int memberLevel, int pistonLevel) {
        this.borderLevel = borderLevel;
        this.memberLevel = memberLevel;
        this.pistonLevel = pistonLevel;
    }

    public int getBorderLevel() {
        return borderLevel;
    }

    public void setBorderLevel(int borderLevel) {
        this.borderLevel = borderLevel;
    }

    public int getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(int memberLevel) {
        this.memberLevel = memberLevel;
    }

    public int getPistonLevel() {
        return pistonLevel;
    }

    public void setPistonLevel(int pistonLevel) {
        this.pistonLevel = pistonLevel;
    }
}
