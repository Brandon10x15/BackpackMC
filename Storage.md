# Storage.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

## Backends

- YAML
    - Stores each backpack in plugins/BackpackMC/<yaml.folder>/<uuid>.yml
    - Contents serialized as Base64
- SQLite
    - File path configured by sqlite.file
    - Table schema:
        - backpacks(uuid TEXT PRIMARY KEY, contents TEXT)
- MySQL
    - Connection parameters configured under storage.mysql
    - Table schema:
        - backpacks(uuid CHAR(36) PRIMARY KEY, contents MEDIUMTEXT)

## Performance and safety

- Synchronous save on flush; asynchronous saves for most operations
- Contents are sanitized to avoid over-stacks and duplication:
    - Amounts clamped to valid stack sizes
    - Extra amounts split across additional slots up to capacity
    - Excess beyond capacity is discarded (never persisted)

## Switching backends

- Set storage.type in config.yml
- Run /backpack migrate <YAML|SQLITE|MYSQL> to copy existing data
- Restart recommended after migration
