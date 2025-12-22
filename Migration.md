# Migration.md

[README](README.md) | [Installation](Installation.md) | [Configuration](Configuration.md) | [Commands](Commands.md) | [Permissions](Permissions.md) | [Shortcut-Item](Shortcut-Item.md) | [Storage](Storage.md) | [Migration](Migration.md) | [API](API.md) | [Events](Events.md) | [FAQ](FAQ.md) | [Troubleshooting](Troubleshooting.md)

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

- Migration uses simple load/save serialization of contents; ensure correct permissions and connectivity.
- Consider backing up your data folder or database before migrating.
