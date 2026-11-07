# Compilar el .jar con GitHub (gratis, sin instalar nada)

1. Crea una cuenta en https://github.com (si no tienes).
2. Arriba a la derecha: "+" > "New repository". Nombre: aurora-client. Marca "Public". Crea el repositorio.
3. Descomprime aurora-client.zip en tu PC. Entra a la carpeta y selecciona TODO su contenido
   (incluida la carpeta oculta .github).
4. En tu repositorio pulsa "uploading an existing file", arrastra todo y pulsa "Commit changes".
   - Si la carpeta .github no se sube: "Add file" > "Create new file", escribe el nombre
     .github/workflows/build.yml y pega el contenido de ese archivo.
5. Ve a la pestaña "Actions". Debe empezar sola una ejecucion llamada "Build mod" (tarda unos minutos).
6. Cuando salga el check verde, abre la ejecucion y abajo, en "Artifacts", descarga aurora-client-jar.
   Dentro del zip esta el .jar.
7. Copia el .jar a .minecraft/mods junto con Fabric API (1.21.11) y abre Minecraft con Fabric.

Si sale una X roja: abre la ejecucion, copia el error del paso "Compilar" y pegalo en Claude.
