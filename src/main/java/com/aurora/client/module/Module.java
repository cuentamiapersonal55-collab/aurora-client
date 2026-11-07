package com.aurora.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Module {
    public enum Category {
        VISUAL("Visual"), UTILITY("Utility"), FUN("Cosmetics & Emotes");
        public final String label;
        Category(String l) { label = l; }
    }

    public final String name;
    public final String desc;
    public final Category category;
    public boolean enabled;

    protected Module(String name, String desc, Category category, boolean enabledByDefault) {
        this.name = name; this.desc = desc; this.category = category; this.enabled = enabledByDefault;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(MinecraftClient mc) {}
    public void onHud(DrawContext ctx, MinecraftClient mc) {}
}
