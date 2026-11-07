package com.aurora.client.module;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.random.Random;

/** Todo el spawn de particulas pasa por aqui: si Mojang renombra el metodo, solo cambias esta linea. */
public final class Fx {
    private Fx() {}

    public static void add(ClientWorld w, ParticleEffect p, double x, double y, double z, double vx, double vy, double vz) {
        w.addParticleClient(p, x, y, z, vx, vy, vz);   // en versiones <=1.21.8 se llamaba addParticle
    }

    public static void burst(ClientWorld w, ParticleEffect p, double x, double y, double z, int n, double speed) {
        Random r = w.random;
        for (int i = 0; i < n; i++) {
            add(w, p, x, y, z,
                (r.nextDouble() - 0.5) * speed,
                (r.nextDouble() - 0.2) * speed,
                (r.nextDouble() - 0.5) * speed);
        }
    }

    public static void ring(ClientWorld w, ParticleEffect p, double cx, double cy, double cz, double radius, int n, double phase, double vy) {
        for (int i = 0; i < n; i++) {
            double a = phase + (Math.PI * 2 * i / n);
            add(w, p, cx + Math.cos(a) * radius, cy, cz + Math.sin(a) * radius, 0, vy, 0);
        }
    }
}
