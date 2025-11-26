package me.mami.stratocraft.util;

import me.mami.stratocraft.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * LangManager - Dil dosyası yönetimi
 */
public class LangManager {
    private final Main plugin;
    private FileConfiguration langConfig;
    private File langFile;

    public LangManager(Main plugin) {
        this.plugin = plugin;
        loadLang();
    }

    public void loadLang() {
        langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        // Varsayılan değerleri yükle
        InputStream defaultStream = plugin.getResource("lang.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)
            );
            langConfig.setDefaults(defaultConfig);
        }
    }

    public String getMessage(String path) {
        String message = langConfig.getString(path);
        if (message == null) {
            return ChatColor.RED + "Missing translation: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }

    public void reloadLang() {
        loadLang();
    }
}

