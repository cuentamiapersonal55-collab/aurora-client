package com.aurora.client.module;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> ALL = new ArrayList<>();

    public static void init() {
        ALL.add(new Modules.AuroraSky());
        ALL.add(new Modules.HitEffects());
        ALL.add(new Modules.KillEffects());
        ALL.add(new Modules.Brightness());
        ALL.add(new Modules.HudInfo());
        ALL.add(new Modules.Zoom());
        ALL.add(new Modules.Halo());
        ALL.add(new Modules.Trail());
        ALL.add(new Modules.Emotes());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> c) {
        for (Module m : ALL) if (c.isInstance(m)) return (T) m;
        throw new IllegalStateException("Module not registered: " + c);
    }
}
