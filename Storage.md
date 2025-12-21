##Links:
- Readme: [README.md](./README.md)
- Installation: [Installation.md](./Installation.md)
- Configuration: [Configuration.md](./Configuration.md)
- Commands: [Commands.md](./Commands.md)
- Permissions: [Permissions.md](./Permissions.md)
- Storage Backends: [Storage.md](./Storage.md)
- Shortcut Item: [Shortcut-Item.md](./Shortcut-Item.md)
- Migration Guide: [Migration.md](./Migration.md)
- Troubleshooting: [Troubleshooting.md](./Troubleshooting.md)
- API (developers): [API.md](./API.md)
- Events (developers): [Events.md](./Events.md)
- FAQ: [FAQ.md](./FAQ.md)

# Storage Backends

BackpackMC supports three backends:

## YAML
- Simple file-based storage under storage.yaml.folder (default “data”)
- Each player stored as <UUID>.yml with Base64 contents string
- Pros: easy to read/backup
- Cons: slower for large servers

## SQLite
- Local database file (settings.storage.sqlite.file)
- Table:
  - uuid TEXT PRIMARY KEY
  - contents TEXT
- Pros: single-file DB, better than YAML for scale
- Cons: local file access

## MySQL
- External database
- Table:
  - uuid CHAR(36) PRIMARY KEY
  - contents MEDIUMTEXT
- Pros: scalable, central DB
- Cons: requires setup and credentials

## Choosing a Backend
- Small servers: YAML or SQLite
- Larger/hosted servers: MySQL

## Migration
Use /backpack migrate <YAML|SQLITE|MYSQL> to convert all existing entries to your target backend. See [Migration.md](./Migration.md).

## Data Format
- Item contents are serialized using BukkitObject streams and encoded in Base64.
- Stored as a single string per backpack.
