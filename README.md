# OneBlock Ultima

## Requirements

| Component | Version | Purpose |
|-----------|---------|---------|
| JDK | **17+** | Running Gradle and RetroFuturaGradle (Minecraft decompilation) |
| JDK | **8** | Compiling mod code (source/target compatibility) |
| Gradle | 8.8 | Build system (included via wrapper) |
| IntelliJ IDEA | 2024+ | Recommended IDE |

> **Note:** JDK 17+ is required to run the Gradle daemon. The mod code itself compiles to Java 8 — RetroFuturaGradle handles this automatically. The JDK 17 path is configured in `gradle.properties` via `org.gradle.java.installations.paths`.

## Environment Setup

### Installing JDK

Download and install:
- **JDK 17** (or newer) — [Adoptium](https://adoptium.net/), [Oracle](https://www.oracle.com/java/technologies/downloads/), or any other distribution
- **JDK 8** — optional, RFG will use JDK 17 and cross-compile to Java 8 automatically

### `gradle.properties` parameters

```properties
org.gradle.jvmargs=-Xmx3G
org.gradle.java.installations.paths=C:\\Program Files\\Java\\jdk-17
```

If JDK 17 is installed elsewhere, update `org.gradle.java.installations.paths` to match your path.

## Build and Run

```bash
# Build the mod JAR
./gradlew build

# Run the tests
./gradlew runTests

# Run the client
./gradlew runClient

# Run the server
./gradlew runServer
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The `run/` directory is created on first launch. If something breaks, delete `run/` before retrying.

## Opening in IntelliJ IDEA

1. File > Open > select the project root
2. Import as a Gradle project when prompted
3. Wait for Gradle sync to complete
4. Settings > Build > Compiler > Java Compiler — set target bytecode version to 8
5. Settings > Build > Build Tools > Gradle > Gradle JDK — select JDK 17

## Project Structure

```
src/main/java/ru/defea/oneblockultima/
  OneBlockUltima.java          — main mod class (@Mod)
  ModGuiFactory.java           — config GUI factory
  block/                       — blocks
  tile/                        — tile entities
  gui/                         — GUI screens
  command/                     — commands
  config/                      — block set configuration
  network/                     — network packets
  world/                       — custom world type

src/main/resources/
  mcmod.info                   — mod metadata
  pack.mcmeta                  — resource pack metadata
  assets/oneblockultima/
    lang/en_US.lang            — English translations
    lang/ru_RU.lang            — Russian translations
```