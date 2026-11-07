# Aurora Client (Fabric, 1.21.11)

Cliente visual / calidad de vida. Todo es del lado del cliente y no da ventaja competitiva.

## Controles
- **Shift derecho**: abrir/cerrar menu
- **C** (mantener): zoom
- **Z / X / V / B**: emotes (corazones / fiesta / espiral / destello)

## Compilar
1. Entra a https://fabricmc.net/develop , elige Minecraft 1.21.11 + Yarn, y copia las versiones
   exactas de `yarn_mappings`, `loader_version`, `loom_version` y `fabric_version` a `gradle.properties`.
2. Necesitas JDK 21 y Gradle (o el wrapper del template oficial de Fabric: copia `gradlew`, `gradlew.bat` y `gradle/`).
3. `./gradlew build`  ->  el jar queda en `build/libs/aurora-client-1.0.0.jar`.
4. Copialo a `.minecraft/mods` junto con Fabric API.

## Si falla la compilacion
No pude compilarlo (sin internet en mi entorno), asi que puede haber 1-2 nombres de API distintos en 1.21.11.
- Particulas: `Fx.java` (unico lugar donde se llama a `addParticleClient`).
- Resto de renders: `HudRenderCallback` esta deprecado en Fabric API reciente; si da error usa `HudElementRegistry`.
