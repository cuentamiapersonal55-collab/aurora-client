package com.aurora.client;

import com.aurora.client.gui.ClickGui;
import com.aurora.client.module.Module;
import com.aurora.client.module.ModuleManager;
import com.aurora.client.module.Modules;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class AuroraClient implements ClientModInitializer {
    private boolean shiftWasDown = false;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        // Menu con Shift derecho (polling directo de GLFW, sin keybinds de Minecraft)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.getWindow() == null) return;
            boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !shiftWasDown && mc.currentScreen == null && mc.world != null) {
                mc.setScreen(new ClickGui());
            }
            shiftWasDown = down;

            for (Module m : ModuleManager.ALL) {
                if (m.enabled) m.onTick(mc);
            }
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            for (Module m : ModuleManager.ALL) {
                if (m.enabled) m.onHud(ctx, mc);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && entity instanceof LivingEntity le) {
                Modules.HitEffects hf = ModuleManager.get(Modules.HitEffects.class);
                if (hf.enabled) hf.onHit(le);
                Modules.KillEffects.track(le);
            }
            return ActionResult.PASS;
        });
    }
}
