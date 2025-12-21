# Migration Guide

Safely migrate your data between YAML, SQLite, and MySQL.

## Preparation
- Back up your current storage:
  - YAML: copy the storage folder
  - SQLite: copy the .db file
  - MySQL: dump the table
- Ensure the target backend is configured in config.yml (host, credentials, file path)

## Command
- Run:
  - /backpack migrate YAML
  - /backpack migrate SQLITE
  - /backpack migrate MYSQL
- Requires: backpack.migrate
- The plugin initializes the target backend, iterates all entries, and saves them to the target.

## After Migration
- Switch storage.type in config.yml to the new backend.
- Restart the server or /backpack reload.
- Spot-check a few users’ backpacks by opening them and verifying contents.

## Notes
- The migration runs asynchronously; you’ll receive a completed count.
- Migration does not delete from the source; keep backups until verified.
