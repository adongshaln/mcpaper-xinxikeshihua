package com.adongcraft.xinxikeshihua;

/** 能决定方块掉落物的工具种类。 */
public enum ToolKind {
    HAND("徒手"),
    PICKAXE("镐"),
    AXE("斧"),
    SHOVEL("锹"),
    HOE("锄"),
    SHEARS("剪刀"),
    SWORD("剑"),
    OTHER("工具"),
    UNKNOWN("正确工具");

    private final String label;

    ToolKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
