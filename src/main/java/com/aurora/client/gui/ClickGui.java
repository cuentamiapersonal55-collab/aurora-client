package com.aurora.client.gui;

import com.aurora.client.module.Module;
import com.aurora.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu con paneles por categoria. Los clics se leen con GLFW en render()
 * para no depender de la firma de mouseClicked (cambio en cada version).
 */
public class ClickGui extends Screen {
    private final long opened = System.currentTimeMillis();
    private boolean wasDown = false;
    private boolean wasShift = true; // evita cerrar al instante

    public ClickGui() { super(Text.literal("Aurora Client")); }

    @Override public boolean shouldPause() { return false; }

    private static int argb(int a, int r, int g, int b) {
        return (Math.max(5, Math.min(255, a)) << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long handle = mc.getWindow().getHandle();

        float a = Math.min(1f, (System.currentTimeMillis() - opened) / 280f);
        a = 1f - (1f - a) * (1f - a); // ease-out

        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean click = down && !wasDown;
        wasDown = down;

        boolean shift = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (shift && !wasShift) { close(); return; }
        wasShift = shift;

        // fondo
        ctx.fillGradient(0, 0, width, height, argb((int) (110 * a), 8, 0, 30), argb((int) (170 * a), 0, 45, 55));

        // titulo
        ctx.drawCenteredTextWithShadow(textRenderer, "AURORA CLIENT", width / 2, 12 - (int) ((1 - a) * 10), argb((int) (255 * a), 170, 255, 220));
        ctx.drawCenteredTextWithShadow(textRenderer, "Click para activar/desactivar · Shift derecho o ESC para cerrar", width / 2, 24, argb((int) (180 * a), 200, 200, 220));

        Module.Category[] cats = Module.Category.values();
        int panelW = 140, gap = 14;
        int totalW = cats.length * panelW + (cats.length - 1) * gap;
        int startX = (width - totalW) / 2;
        int baseY = 45 + (int) ((1 - a) * 25);
        String hoverDesc = null;

        for (int c = 0; c < cats.length; c++) {
            List<Module> mods = new ArrayList<>();
            for (Module m : ModuleManager.ALL) if (m.category == cats[c]) mods.add(m);

            int x = startX + c * (panelW + gap);
            int rowH = 18;
            int h = 20 + mods.size() * rowH + 4;

            ctx.fill(x, baseY, x + panelW, baseY + h, argb((int) (200 * a), 14, 14, 26));
            ctx.fillGradient(x, baseY, x + panelW, baseY + 20, argb((int) (255 * a), 40, 190, 140), argb((int) (255 * a), 130, 80, 220));
            ctx.drawText(textRenderer, cats[c].label, x + 8, baseY + 6, argb((int) (255 * a), 255, 255, 255), true);

            for (int i = 0; i < mods.size(); i++) {
                Module m = mods.get(i);
                int ry = baseY + 22 + i * rowH;
                boolean hover = mx >= x && mx <= x + panelW && my >= ry && my < ry + rowH;
                if (hover) {
                    hoverDesc = m.desc;
                    if (click) m.toggle();
                }
                int bg = m.enabled ? argb((int) (160 * a), 40, 150, 120) : argb((int) (hover ? 120 : 60 * a), 60, 60, 80);
                ctx.fill(x + 3, ry, x + panelW - 3, ry + rowH - 2, bg);
                // indicador
                ctx.fill(x + panelW - 14, ry + 4, x + panelW - 8, ry + 10,
                    m.enabled ? argb((int) (255 * a), 120, 255, 190) : argb((int) (255 * a), 90, 90, 110));
                ctx.drawText(textRenderer, m.name, x + 9, ry + 5, argb((int) (255 * a), 255, 255, 255), true);
            }
        }

        if (hoverDesc != null) {
            ctx.drawCenteredTextWithShadow(textRenderer, hoverDesc, width / 2, height - 20, 0xFFFFFFFF);
        }
    }
}
