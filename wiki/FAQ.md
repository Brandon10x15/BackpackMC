<!-- FAQ.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## How do I change backpack size?
- Grant permissions backpackmc.backpack.size.N (1–6). Highest N the player has decides rows. If the player has use but no size perms, they get 1 row.

## Why can’t the backpack open in some worlds or gamemodes?
- Check settings.worlds-blacklist and settings.restrict-gamemodes in config.yml.
- Admins can bypass via backpackmc.backpack.ignoreWorldBlacklist and backpackmc.backpack.ignoreGameMode.

## How do I enable auto-pickup to backpack?
- Ensure settings.auto-pickup-enabled is true and the player has backpackmc.backpack.fullpickup.

## Can I block certain items from being stored?
- Add materials to settings.item-filter.blocked. Players will be notified if they try to store blocked items.

## Does the shortcut item get dropped?
- Not by default. Set shortcut-item.droppable to true to allow dropping.

## How do I migrate storage?
- Use /backpack migrate <YAML|SQLITE|MYSQL> after configuring the target backend. Then set storage.type and restart.

## Are dupes prevented?
- Interactions that cause duplication (collect-to-cursor/double-click/middle-click in the backpack GUI, right-click drag spread) are cancelled.
- Contents are sanitized to valid stack sizes on snapshot and save.
- Crafting is refill-after-craft only with a re-entrancy lock to prevent duplication.

## How do I keep backpacks on death?
- Global default is settings.keep-on-death-default. Players with backpackmc.backpack.keepOnDeath always keep their backpack.
- If not keeping and settings.drop-on-death-if-not-keeping is true, contents will drop on death.

## How can I update?
- Enable settings.updater.enabled and set settings.updater.github-repo (e.g., Brandon10x15/BackpackMC).
- Optionally set include-prereleases and auto-download.
- Use /backpack update to check, and /backpack update download to fetch to the server update folder.
- check-url and download-url are optional fallbacks if GitHub is unavailable.