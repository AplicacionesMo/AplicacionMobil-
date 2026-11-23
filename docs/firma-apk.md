# Generación del APK firmado

Android solo instala aplicaciones firmadas. La firma prueba **quién** publicó la app y que nadie la
modificó: si alguien altera el APK, la firma deja de coincidir y Android lo rechaza. Las
actualizaciones deben firmarse con la **misma llave**; si se pierde, no se puede actualizar la app
publicada.

## Archivos involucrados

| Archivo | Qué es |
|---|---|
| `app/keystore/tacticaldev-release.jks` | Almacén de llaves (keystore). Contiene la llave privada con alias `tacticaldev` |
| `keystore.properties` | Ruta del .jks, alias y contraseñas. Lo lee Gradle |
| `app/build.gradle.kts` → `signingConfigs.release` | Le dice a Gradle con qué llave firmar el build `release` |
| `apk/ServicioTecnico-v2.0-release.apk` | El APK final, firmado |

Datos de la llave: RSA 2048 bits, validez 10.000 días, `CN=TacticalDEV Servicio Tecnico, O=Duoc UC, L=Santiago, C=CL`.

## Opción A — Android Studio (la que se muestra en la defensa)

1. **Build → Generate Signed App Bundle / APK…**
2. Elegir **APK** → Next.
3. **Key store path**: `app/keystore/tacticaldev-release.jks` (botón *Choose existing…*).
   - Para crear una llave nueva: *Create new…*, indicar ruta, contraseña, alias, validez (25 años) y
     los datos del certificado (nombre, organización, ciudad, país).
4. Contraseña del almacén y de la llave: `TacticalDev2026` · Alias: `tacticaldev` → Next.
5. Build variant: **release** → Create.
6. Android Studio avisa "APK(s) generated successfully" → *locate*. Queda en `app/release/app-release.apk`.
7. Copiarlo a `apk/ServicioTecnico-v2.0-release.apk` y hacer commit.

## Opción B — Gradle (usa la configuración del build.gradle)

```bash
./gradlew assembleRelease
# Resultado: app/build/outputs/apk/release/app-release.apk
```

Funciona porque `build.gradle.kts` lee `keystore.properties` y asigna
`signingConfig = signingConfigs.getByName("release")` al build type `release`.

## Cómo se creó la llave por consola (equivalente a "Create new…")

```bash
keytool -genkeypair -v -keystore app/keystore/tacticaldev-release.jks -storetype JKS \
  -alias tacticaldev -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass TacticalDev2026 -keypass TacticalDev2026 \
  -dname "CN=TacticalDEV Servicio Tecnico, OU=Desarrollo de Aplicaciones Moviles, O=Duoc UC, L=Santiago, ST=Region Metropolitana, C=CL"
```

## Verificar que el APK está firmado

```bash
# Muestra el certificado con que se firmó (requiere las build-tools del SDK de Android)
apksigner verify --print-certs apk/ServicioTecnico-v2.0-release.apk

# Ver el contenido de la llave
keytool -list -v -keystore app/keystore/tacticaldev-release.jks -storepass TacticalDev2026
```

La huella SHA-256 del certificado que muestra `apksigner` debe ser la misma que muestra `keytool`.

## Antes de instalar el APK de release

- El APK apunta a `10.0.2.2` (el PC visto desde el emulador). Para un teléfono físico, cambiar las
  URL en `build.gradle.kts` por la IP del PC antes de generarlo.
- Si el emulador ya tiene la versión de depuración instalada, desinstalarla: tienen firmas distintas.
- Instalar arrastrando el APK al emulador o con `adb install apk/ServicioTecnico-v2.0-release.apk`.

## Capturas para el README

Guardar en `docs/capturas/`: `apk-firmado.png` (mensaje de éxito o salida de `apksigner`) y
`jks.png` (el archivo .jks en el proyecto o la salida de `keytool -list`).
