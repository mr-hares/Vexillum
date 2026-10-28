package ru.mr_hares.vexillum.loaders;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MessagesLoader {
    private YamlConfiguration yaml = null;
    private final JavaPlugin plugin;

    public MessagesLoader(JavaPlugin plugin) throws NullPointerException {
        this.plugin = plugin;
        for (String file: getMessageFiles(plugin)) {
            File f = new File(plugin.getDataFolder(), "messages/" + file);
            if (!f.exists()) plugin.saveResource("messages/" + file, false);

            String lang = plugin.getConfig().getString("lang", "en_us");
            if (Objects.equals(lang + ".yml", file)) {
                yaml = YamlConfiguration.loadConfiguration(f);
                plugin.getLogger().warning("Selected language: " + file);
            }
        }

        if (yaml == null) {
            try (InputStream defMessage = plugin.getResource("messages/en_us.yml")) {
                assert defMessage != null;
                try (InputStreamReader reader = new InputStreamReader(defMessage, StandardCharsets.UTF_8)) {
                    yaml = YamlConfiguration.loadConfiguration(reader);
                }
                plugin.getLogger().warning("Selected language: en_us.yml");
            } catch (Exception ignored) {}
        }

        if (yaml == null) throw new NullPointerException();
    }

    public String reload() {
        for (String file: getMessageFiles(plugin)) {
            File f = new File(plugin.getDataFolder(), "messages/" + file);
            if (!f.exists()) plugin.saveResource("messages/" + file, false);

            String lang = plugin.getConfig().getString("lang", "en_us");
            if (Objects.equals(lang + ".yml", file)) {
                yaml = YamlConfiguration.loadConfiguration(f);
                return lang;
            }
        }

        if (yaml == null) {
            try (InputStream defMessage = plugin.getResource("messages/en_us.yml")) {
                assert defMessage != null;
                try (InputStreamReader reader = new InputStreamReader(defMessage, StandardCharsets.UTF_8)) {
                    yaml = YamlConfiguration.loadConfiguration(reader);
                }
                return "en_us";
            } catch (Exception ignored) {}
        }

        return "null";
    }

    public String getString(String path) {
        if (yaml == null) throw new NullPointerException();

        String lang = plugin.getConfig().getString("lang", "en_us");
        try (InputStream defMessage = plugin.getResource("messages/" + lang + ".yml")) {
            assert defMessage != null;
            try (InputStreamReader reader = new InputStreamReader(defMessage, StandardCharsets.UTF_8)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                return yaml.getString(path, defaultConfig.getString(path));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getStringList(String path) {
        if (yaml == null) throw new NullPointerException();

        String lang = plugin.getConfig().getString("lang", "en_us");
        try (InputStream defMessage = plugin.getResource("messages/" + lang + ".yml")) {
            assert defMessage != null;
            try (InputStreamReader reader = new InputStreamReader(defMessage, StandardCharsets.UTF_8)) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(reader);
                List<String> result = yaml.getStringList(path);
                return result.isEmpty() ? defaultConfig.getStringList(path) :
                        result;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> getMessageFiles(JavaPlugin plugin) {
        List<String> filePaths = new ArrayList<>();
        String targetFolder = "messages/";

        try {
            URL jarUrl = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            JarFile jarFile = new JarFile(jarUrl.getPath());

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(targetFolder) && !entry.isDirectory()) {
                    String resourcePath = name.substring(targetFolder.length());
                    filePaths.add(resourcePath);
                }
            }
            jarFile.close();
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось прочитать JAR-файл: " + e.getMessage());
        }
        return filePaths;
    }

    public YamlConfiguration getYaml() {
        return yaml;
    }
}
