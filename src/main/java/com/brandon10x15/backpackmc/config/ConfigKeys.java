// src/main/java/com/brandon10x15/backpackmc/config/ConfigKeys.java
package com.brandon10x15.backpackmc.config;

public final class ConfigKeys {
    private ConfigKeys() {
    }

    public static final String LANGUAGE = "language";
    public static final String STORAGE_TYPE = "storage.type";
    public static final String MYSQL_HOST = "storage.mysql.host";
    public static final String MYSQL_PORT = "storage.mysql.port";
    public static final String MYSQL_DB = "storage.mysql.database";
    public static final String MYSQL_USER = "storage.mysql.user";
    public static final String MYSQL_PASS = "storage.mysql.password";
    public static final String MYSQL_SSL = "storage.mysql.useSSL";
    public static final String SQLITE_FILE = "storage.sqlite.file";
    public static final String YAML_FOLDER = "storage.yaml.folder";
    public static final String SETTINGS_COOLDOWN = "settings.command-cooldown-seconds";
    public static final String SETTINGS_BLACKLIST = "settings.worlds-blacklist";
    public static final String SETTINGS_RESTRICT_GMS = "settings.restrict-gamemodes";
    public static final String SETTINGS_AUTOPICKUP = "settings.auto-pickup-enabled";
    public static final String SETTINGS_ITEMFILTER_BLOCKED = "settings.item-filter.blocked";
    public static final String SETTINGS_KEEP_ON_DEATH_DEFAULT = "settings.keep-on-death-default";
    public static final String SETTINGS_DROP_ON_DEATH_IF_NOT_KEEPING = "settings.drop-on-death-if-not-keeping";
    public static final String UPDATER_ENABLED = "settings.updater.enabled";

    // New GitHub-based updater keys
    public static final String UPDATER_GITHUB_REPO = "settings.updater.github-repo";
    public static final String UPDATER_INCLUDE_PRERELEASES = "settings.updater.include-prereleases";

    // NEW: auto-download toggle
    public static final String UPDATER_AUTO_DOWNLOAD = "settings.updater.auto-download";

    // Legacy (optional fallback) updater keys
    public static final String UPDATER_CHECK_URL = "settings.updater.check-url";
    public static final String UPDATER_DOWNLOAD_URL = "settings.updater.download-url";

    public static final String SHORTCUT_ENABLED = "shortcut-item.enabled";
    public static final String SHORTCUT_GIVE_ON_JOIN = "shortcut-item.give-on-join";
    public static final String SHORTCUT_FORCE_SLOT = "shortcut-item.force-slot";
    public static final String SHORTCUT_MATERIAL = "shortcut-item.material";
    public static final String SHORTCUT_NAME = "shortcut-item.name";
    public static final String SHORTCUT_LORE = "shortcut-item.lore";
    public static final String SHORTCUT_DROPPABLE = "shortcut-item.droppable";

    // Auto-sort
    public static final String SETTINGS_AUTOSORT_DEFAULT = "settings.auto-sort.default-mode";

    // Clear inventory confirmation default
    public static final String SETTINGS_CLEAR_CONFIRM_DEFAULT = "settings.clear-inventory.confirmation-default-enabled";
}
