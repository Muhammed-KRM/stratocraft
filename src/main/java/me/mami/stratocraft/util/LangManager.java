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
        return colorize(message);
    }
    
    /**
     * Hex renk desteği ile mesajı renklendir
     */
    private String colorize(String message) {
        // Hex renk kodu desteği (Örn: &#FF0000 veya #FF0000)
        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("&#([a-fA-F0-9]{6})");
        java.util.regex.Matcher hexMatcher = hexPattern.matcher(message);
        
        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1);
            try {
                // Bungee ChatColor kullanarak hex renk uygula
                net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.of("#" + hexColor);
                message = message.replace("&#" + hexColor, color.toString());
            } catch (Exception e) {
                // Hex renk parse edilemezse eski halini bırak
            }
        }
        
        // Standart Minecraft renk kodlarını çevir (&c, &a, vb.)
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

