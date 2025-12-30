<!-- Installation.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## Requirements
- Minecraft server software supporting API 1.21 (Paper recommended)
- Java 21

## Steps
- Download BackpackMC.jar and place it in your server's plugins folder.
- Start the server once to generate config.yml and message files.
- Configure:
    - Edit plugins/BackpackMC/config.yml (language, storage, restrictions, updater).
    - Languages live in plugins/BackpackMC/messages/en_US.yml and messages/de_DE.yml. Missing keys auto-merge on boot.
- Restart or use the reload command:
    - /backpack reload

## Verifying
- Ensure the console logs “BackpackMC enabled.”
- Run /backpack in-game with a player who has backpackmc.backpack.use.
- If shortcut-item.give-on-join is true, join the server to receive the Backpack bundle in your inventory.

## Upgrading
- Replace the jar file.
- On next boot, config and message defaults merge any new keys while preserving your values.
- If updater.enabled and updater.auto-download are true, the plugin can auto-download updates to the server's update folder for next restart.
- If you changed storage type, run Migration (wiki/Migration.md).
