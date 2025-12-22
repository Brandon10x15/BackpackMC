# Installation.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

## Requirements

- Minecraft server software supporting API 1.20 (Paper or Spigot recommended)
- Java 17 or newer

## Steps

- Download BackpackMC.jar and place it in your server's plugins folder.
- Start the server once to generate config.yml and messages files.
- Configure:
    - Open plugins/BackpackMC/config.yml and adjust settings (language, storage, restrictions).
    - Languages live in plugins/BackpackMC/messages/en_US.yml and messages/de_DE.yml. Missing keys auto-merge on boot.
- Restart or use the reload command:
    - /backpack reload

## Verifying

- Ensure the console logs “BackpackMC enabled.”
- Run /backpack in-game with a player who has backpackmc.backpack.use.
- If shortcut-item.give-on-join is true, join the server to receive the Backpack bundle in your inventory.

## Upgrading

- Replace the jar file.
- On next boot, config defaults and message defaults merge in any new keys while preserving your values.
- If you changed storage type, run Migration (Migration.md).
