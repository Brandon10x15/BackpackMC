<!-- Migration.md -->

[README](README.md) | [Installation](wiki/Installation.md) | [Configuration](wiki/Configuration.md) | [Commands](wiki/Commands.md) | [Permissions](wiki/Permissions.md) | [Shortcut-Item](wiki/Shortcut-Item.md) | [Storage](wiki/Storage.md) | [Migration](wiki/Migration.md) | [API](wiki/API.md) | [Events](wiki/Events.md) | [FAQ](wiki/FAQ.md) | [Troubleshooting](wiki/Troubleshooting.md)

## Migration command
- /backpack migrate <YAML|SQLITE|MYSQL>
- Requires backpackmc.backpack.migrate
- Copies all records listed by the current backend into the target backend

## Steps to migrate
- Configure target backend settings in config.yml (e.g., MySQL credentials or SQLite file).
- Run the migrate command.
- Observe migration count feedback.
- Update storage.type to the new backend in config.yml.
- Restart the server.

## Notes
- Migration uses load/save serialization of contents; ensure correct permissions and connectivity.
- Shortcut items (the backpack shortcut) are stripped from stored contents during migration.
- Consider backing up your data folder or database before migrating.
