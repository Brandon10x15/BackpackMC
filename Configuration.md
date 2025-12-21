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

# Configuration

The default config.yml keys and behavior:

## language
- Set language file name, e.g., en_US or de_DE.
- Corresponding file must exist under messages/<lang>.yml.

## storage
- type: YAML | SQLITE | MYSQL
- mysql:
  - host, port, database, user, password, useSSL
- sqlite:
  - file: data.db
- yaml:
  - folder: data

## settings
- command-cooldown-seconds: cooldown for /backpack self-use
- worlds-blacklist: worlds where backpacks cannot be used (unless bypass permission)
- restrict-gamemodes: gamemodes disallowed for opening backpacks (unless bypass)
- auto-pickup-enabled: enables full-inventory pickup into backpacks for players with backpack.fullpickup
- item-filter.blocked: Materials that cannot be stored in backpacks (e.g., BEDROCK, COMMAND_BLOCK)
- keep-on-death-default:
  - true: keep contents on death unless overridden
  - false: clear on death unless player has backpack.keepOnDeath
- drop-on-death-if-not-keeping:
  - true: drops backpack contents on the ground if not keeping
  - false: clears without dropping
- auto-sort.default-mode: OFF | LIGHT | AGGRESSIVE (initial per-player preference)
- updater:
  - enabled: enables update checks
  - check-url: returns latest version string
  - download-url: info link for updates

## shortcut-item
- enabled: toggles shortcut item feature
- give-on-join: give item automatically to eligible players
- force-slot: -1 for no force; 0–8 to enforce hotbar slot
- material: default BUNDLE; may be set to another Material
- name: display name (supports color codes with &)
- lore: lines below name (supports color codes)
- droppable: allow dropping shortcut item (Q); default false

## Messages
- Customize messages/en_US.yml or messages/de_DE.yml.
- Switch language via config.yml: language: de_DE (for example).
- Use color codes with &.

## Reloading
- Use /backpack reload to reload config and messages.
