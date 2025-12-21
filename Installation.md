# Installation

## Requirements
- Paper/Spigot 1.21+ (api-version: 1.21)
- Java 17+

## Steps
1. Place BackpackMC.jar in your server’s plugins/ directory.
2. Start the server to generate:
   - config.yml
   - messages/en_US.yml (and messages/de_DE.yml if bundled)
   - storage folders/files (depending on backend)
3. Open config.yml and choose storage backend:
   - YAML (default, simple files)
   - SQLite (local database file)
   - MySQL (external database)
4. Configure optional features:
   - language
   - item blacklist
   - auto-pickup
   - auto-sort default mode
   - shortcut item behavior
5. Restart the server or run /backpack reload after edits.

## MySQL Setup
- In config.yml:
  - storage.type: MYSQL
  - storage.mysql.host: your-host
  - storage.mysql.port: 3306
  - storage.mysql.database: backpackmc
  - storage.mysql.user, storage.mysql.password
  - storage.mysql.useSSL: true/false
- The plugin will create table “backpacks” if missing:
  - uuid CHAR(36) PRIMARY KEY
  - contents MEDIUMTEXT (Base64 serialized ItemStacks)

## SQLite Setup
- In config.yml:
  - storage.type: SQLITE
  - storage.sqlite.file: data.db (relative to plugin data folder)
- The plugin will create table “backpacks” if missing:
  - uuid TEXT PRIMARY KEY
  - contents TEXT (Base64 serialized ItemStacks)

## YAML Setup
- In config.yml:
  - storage.type: YAML
  - storage.yaml.folder: data
- Backpacks stored as one .yml per player (UUID.yml) under the folder.

## Verifying Installation
- Join the server with a test account.
- Ensure the player has backpack.use and a size permission (e.g., backpack.size.3).
- Use /backpack, place items, relog, confirm persistence.
- Try autosort and auto-pickup if enabled.
