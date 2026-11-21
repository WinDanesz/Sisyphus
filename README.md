# Sisyphus Backport &nbsp; [![Build Mod](https://github.com/WinDanesz/Sisyphus/actions/workflows/gradle.yml/badge.svg)](https://github.com/WinDanesz/Sisyphus/actions/workflows/gradle.yml) [![Discord](https://img.shields.io/discord/544897694448091146?color=7289DA&label=Discord)](https://discord.gg/wuSsgKwAKv)

A Forge 1.12.2 mod that backports blocks, items, mobs and world generation from newer Minecraft versions, from the Update Aquatic onwards. 

Requires [AzureLib](https://www.curseforge.com/minecraft/mc-mods/azurelib) 1.1.5 or newer.

## Development setup (IntelliJ IDEA)

1. Clone the repository and open the folder in IntelliJ IDEA as a Gradle project.
2. In `Settings > Build, Execution, Deployment > Build Tools > Gradle`, set the Gradle JVM to JDK 25. The Java 8 toolchain that compiles and runs the mod is downloaded automatically.
3. Let the Gradle sync finish. The first sync decompiles Minecraft and takes a while.
4. Start the game with the generated `1. Run Client` / `2. Run Server` run configurations, or with `gradlew runClient` / `gradlew runServer`.

Build the jar with `gradlew build`. It ends up in `build/libs`.

## License

The mod is source-available under [Dan's Mods License (DML) 1.0](LICENSE). You may fork it, build it, change it and send pull requests. You may also run a modified build on your own server and share it with that server's players, but builds of modified versions may not be published on CurseForge, Modrinth or anywhere else public. Official releases may be included in modpacks, but not re-uploaded on their own. Contributors keep the copyright of their work and are credited in CREDITS.md. If the mod goes two years without a release or a commit, it also becomes available under GPL-3.0.
