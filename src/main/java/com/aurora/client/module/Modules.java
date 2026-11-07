package com.aurora.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class Modules {
    private Modules() {}

    static boolean keyDown(MinecraftClient mc, int key) {
        return GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }

    // ------------------------------------------------------------------ VISUAL

    /** Auroras boreales: capa en pantalla que aparece de noche cuando miras hacia arriba. */
    public static class AuroraSky extends Module {
        private float strength = 0f;
        public boolean always = false; // true = tambien de dia (cambia en el codigo si quieres)

        public AuroraSky() { super("Aurora Sky", "Auroras en el cielo (miras arriba de noche)", Category.VISUAL, true); }

        @Override
        public void onHud(DrawContext ctx, MinecraftClient mc) {
            if (mc.world == null || mc.player == null || mc.currentScreen != null || mc.options.hudHidden) return;

            long t = mc.world.getTimeOfDay() % 24000L;
            boolean night = t > 12500 && t < 23500;
            float pitch = mc.player.getPitch(); // -90 = mirando arriba
            float look = MathHelper.clamp((20f - pitch) / 70f, 0f, 1f);
            float target = (night || always) ? look : 0f;
            strength += (target - strength) * 0.05f; // fade suave
            if (strength < 0.01f) return;

            int w = ctx.getScaledWindowWidth();
            int h = ctx.getScaledWindowHeight();
            float time = (System.currentTimeMillis() % 1_000_000L) / 16f;

            // dos capas para dar profundidad
            for (int layer = 0; layer < 2; layer++) {
                float speed = layer == 0 ? 0.020f : 0.013f;
                float yScale = layer == 0 ? 0.55f : 0.40f;
                for (int x = 0; x < w; x += 3) {
                    float wave = 0.5f + 0.5f * MathHelper.sin(x * 0.013f + time * speed + layer * 2f);
                    float wave2 = 0.5f + 0.5f * MathHelper.sin(x * 0.031f - time * speed * 1.7f);
                    float bandH = h * yScale * (0.35f + 0.65f * wave);
                    float hue = 0.30f + 0.25f * MathHelper.sin(x * 0.006f + time * 0.006f + layer); // verde -> violeta
                    int rgb = MathHelper.hsvToRgb(hue, 0.75f, 1f);
                    int a = (int) (strength * (layer == 0 ? 120 : 70) * (0.4f + 0.6f * wave2));
                    a = Math.max(a, 0);
                    int top = (a << 24) | (rgb & 0xFFFFFF);
                    int bot = (rgb & 0xFFFFFF);
                    ctx.fillGradient(x, (int) (bandH * 0.15f), x + 3, (int) bandH, bot, top);
                    ctx.fillGradient(x, 0, x + 3, (int) (bandH * 0.15f), top, top);
                }
            }
        }
    }

    public static class Brightness extends Module {
        private boolean added = false;
        public Brightness() { super("Brightness", "Vision nocturna visual constante", Category.VISUAL, false); }

        @Override
        public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null) return;
            if (!p.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false, false));
                added = true;
            }
        }

        @Override
        public void onDisable() {
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (p != null && added) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
            added = false;
        }
    }

    // ------------------------------------------------------------------ EFECTOS

    public static class HitEffects extends Module {
        public HitEffects() { super("Hit Effects", "Particulas al golpear entidades", Category.VISUAL, true); }

        public void onHit(LivingEntity e) {
            ClientWorld w = MinecraftClient.getInstance().world;
            if (w == null) return;
            double x = e.getX(), y = e.getBodyY(0.6), z = e.getZ();
            Fx.burst(w, ParticleTypes.CRIT, x, y, z, 10, 0.7);
            Fx.burst(w, ParticleTypes.ENCHANTED_HIT, x, y, z, 8, 0.6);
            Fx.ring(w, ParticleTypes.END_ROD, x, y, z, 0.5, 10, w.getTime() * 0.3, 0.02);
        }
    }

    public static class KillEffects extends Module {
        private static LivingEntity target;
        private static long hitTime;
        private static double lx, ly, lz;

        public KillEffects() { super("Kill Effects", "Explosion visual al matar", Category.VISUAL, true); }

        public static void track(LivingEntity e) {
            target = e;
            hitTime = System.currentTimeMillis();
        }

        @Override
        public void onTick(MinecraftClient mc) {
            ClientWorld w = mc.world;
            if (w == null || target == null) return;
            if (System.currentTimeMillis() - hitTime > 8000) { target = null; return; }

            if (target.isRemoved() || target.isDead() || target.getHealth() <= 0f) {
                Fx.burst(w, ParticleTypes.TOTEM_OF_UNDYING, lx, ly, lz, 40, 0.9);
                Fx.burst(w, ParticleTypes.FIREWORK, lx, ly, lz, 25, 0.5);
                for (int i = 0; i < 3; i++) {
                    Fx.ring(w, ParticleTypes.END_ROD, lx, ly - 0.4 + i * 0.4, lz, 0.4 + i * 0.35, 14, 0, 0.03);
                }
                target = null;
            } else {
                lx = target.getX(); ly = target.getBodyY(0.5); lz = target.getZ();
            }
        }
    }

    // ------------------------------------------------------------------ UTILIDAD

    public static class HudInfo extends Module {
        private final long[] clicks = new long[64];
        private int clickIdx = 0;
        private boolean wasDown = false;

        public HudInfo() { super("HUD", "FPS, CPS y coordenadas", Category.UTILITY, true); }

        @Override
        public void onTick(MinecraftClient mc) {
            boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            if (down && !wasDown) clicks[clickIdx++ & 63] = System.currentTimeMillis();
            wasDown = down;
        }

        private int cps() {
            long now = System.currentTimeMillis();
            int n = 0;
            for (long c : clicks) if (now - c < 1000) n++;
            return n;
        }

        @Override
        public void onHud(DrawContext ctx, MinecraftClient mc) {
            if (mc.player == null || mc.options.hudHidden) return;
            String[] lines = {
                "FPS  " + mc.getCurrentFps(),
                "CPS  " + cps(),
                String.format("XYZ  %.0f / %.0f / %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ())
            };
            int x = 6, y = 6, w = 110, h = 14 + lines.length * 11;
            ctx.fill(x, y, x + w, y + h, 0x90101018);
            ctx.fillGradient(x, y, x + w, y + 2, 0xFF55FFAA, 0xFFAA66FF);
            ctx.drawText(mc.textRenderer, "Aurora Client", x + 5, y + 5, 0xFFB8FFE0, true);
            for (int i = 0; i < lines.length; i++) {
                ctx.drawText(mc.textRenderer, lines[i], x + 5, y + 17 + i * 11, 0xFFFFFFFF, true);
            }
        }
    }

    /** Mantener C para hacer zoom. */
    public static class Zoom extends Module {
        private Integer prevFov = null;
        public Zoom() { super("Zoom", "Mantén C para acercar", Category.UTILITY, true); }

        @Override
        public void onTick(MinecraftClient mc) {
            boolean hold = mc.currentScreen == null && keyDown(mc, GLFW.GLFW_KEY_C);
            if (hold && prevFov == null) {
                prevFov = mc.options.getFov().getValue();
                mc.options.getFov().setValue(30);
            } else if (!hold && prevFov != null) {
                mc.options.getFov().setValue(prevFov);
                prevFov = null;
            }
        }

        @Override
        public void onDisable() {
            if (prevFov != null) {
                MinecraftClient.getInstance().options.getFov().setValue(prevFov);
                prevFov = null;
            }
        }
    }

    // ------------------------------------------------------------------ COSMETICOS

    public static class Halo extends Module {
        public Halo() { super("Halo", "Anillo de luz sobre tu cabeza (3ª persona)", Category.FUN, false); }

        @Override
        public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null || mc.world == null || mc.options.getPerspective().isFirstPerson()) return;
            if (mc.world.getTime() % 2 != 0) return;
            double ph = mc.world.getTime() * 0.15;
            Fx.ring(mc.world, ParticleTypes.END_ROD, p.getX(), p.getY() + p.getHeight() + 0.25, p.getZ(), 0.35, 6, ph, 0.0);
        }
    }

    public static class Trail extends Module {
        public Trail() { super("Trail", "Estela de chispas al moverte", Category.FUN, false); }

        @Override
        public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            if (p == null || mc.world == null) return;
            if (p.getVelocity().horizontalLengthSquared() < 0.005) return;
            Fx.add(mc.world, ParticleTypes.FIREWORK, p.getX(), p.getY() + 0.1, p.getZ(), 0, 0.02, 0);
        }
    }

    /** Emotes: Z corazones, X fiesta, V espiral, B destello. Solo visuales. */
    public static class Emotes extends Module {
        private int active = 0;   // 0 = ninguno
        private int ticks = 0;
        private final boolean[] was = new boolean[4];
        private static final int[] KEYS = {GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_B};

        public Emotes() { super("Emotes", "Z corazones · X fiesta · V espiral · B destello", Category.FUN, true); }

        @Override
        public void onTick(MinecraftClient mc) {
            ClientPlayerEntity p = mc.player;
            ClientWorld w = mc.world;
            if (p == null || w == null) return;

            if (mc.currentScreen == null) {
                for (int i = 0; i < 4; i++) {
                    boolean d = keyDown(mc, KEYS[i]);
                    if (d && !was[i]) { active = i + 1; ticks = 0; p.swingHand(Hand.MAIN_HAND); }
                    was[i] = d;
                }
            }
            if (active == 0) return;

            double x = p.getX(), y = p.getY() + p.getHeight() + 0.3, z = p.getZ();
            switch (active) {
                case 1 -> { // corazones
                    if (ticks % 3 == 0) Fx.burst(w, ParticleTypes.HEART, x, y, z, 2, 0.3);
                    if (ticks > 40) active = 0;
                }
                case 2 -> { // fiesta
                    Fx.burst(w, ParticleTypes.FIREWORK, x, y, z, 3, 0.6);
                    if (ticks % 6 == 0) Fx.burst(w, ParticleTypes.NOTE, x, y, z, 1, 0.4);
                    if (ticks > 35) active = 0;
                }
                case 3 -> { // espiral
                    double a = ticks * 0.45;
                    double r = 0.8;
                    for (int k = 0; k < 2; k++) {
                        double aa = a + k * Math.PI;
                        Fx.add(w, ParticleTypes.END_ROD, x + Math.cos(aa) * r, p.getY() + (ticks % 40) * 0.06, z + Math.sin(aa) * r, 0, 0.02, 0);
                    }
                    if (ticks > 60) active = 0;
                }
                case 4 -> { // destello
                    if (ticks == 0) {
                        Fx.burst(w, ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 25, 0.8);
                        Fx.ring(w, ParticleTypes.END_ROD, x, y - 0.5, z, 0.9, 20, 0, 0.02);
                    }
                    if (ticks > 5) active = 0;
                }
                default -> active = 0;
            }
            ticks++;
        }
    }
}
