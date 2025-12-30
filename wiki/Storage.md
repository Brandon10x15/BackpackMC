<!-- Storage.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## Backends
- YAML
    - Stores each backpack in plugins/BackpackMC/<yaml.folder>/<uuid>.yml
    - Contents serialized as Base64
- SQLite
    - File path configured by sqlite.file
    - Table schema: backpacks(uuid TEXT PRIMARY KEY, contents TEXT)
- MySQL
    - Connection parameters configured under storage.mysql
    - Fresh connection per operation (robust against wait_timeout)
    - Table schema: backpacks(uuid CHAR(36) PRIMARY KEY, contents MEDIUMTEXT)

## Performance and safety
- Synchronous save when closing the backpack GUI to avoid race conditions
- Asynchronous saves for most other operations
- Contents are sanitized to avoid over-stacks and duplication:
    - Amounts clamped to valid stack sizes
    - Extra amounts split across additional slots up to capacity
    - Excess beyond capacity is discarded (never persisted)
    - Backpack shortcut items are removed from backpack contents (cannot be stored inside)

## Switching backends
- Set storage.type in config.yml
- Run /backpack migrate <YAML|SQLITE|MYSQL> to copy existing data
- Migration strips any stored shortcut items to prevent recursion
- Restart recommended after migration
