package Golden.Velocity.utils;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager {
    private final Path dataDirectory;
    private final ProxyServer proxy;
    private final PluginContainer plugin;

    private Map<String, Object> config;
    private Map<String, Object> messages;
    private Map<String, Object> anuncios;

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public ConfigManager(Path dataDirectory, ProxyServer proxy, PluginContainer plugin) {
        this.dataDirectory = dataDirectory;
        this.proxy = proxy;
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            copyDefaultConfig("config.yml");
            copyDefaultConfig("messages.yml");
            copyDefaultConfig("anuncios.yml");

            Yaml yaml = new Yaml();
            config = yaml.load(new FileInputStream(dataDirectory.resolve("config.yml").toFile()));
            messages = yaml.load(new FileInputStream(dataDirectory.resolve("messages.yml").toFile()));
            anuncios = yaml.load(new FileInputStream(dataDirectory.resolve("anuncios.yml").toFile()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyDefaultConfig(String fileName) throws IOException {
        Path configPath = dataDirectory.resolve(fileName);
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(fileName)) {
                if (in != null) {
                    Files.copy(in, configPath);
                }
            }
        }
    }

    public void reload() {
        loadConfigs();
    }

    public Component getMessage(String path) {
        Object value = getFromPath(messages, path);

        if (value instanceof List) {
            List<?> lines = (List<?>) value;
            StringBuilder combined = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                combined.append(lines.get(i).toString());
                if (i < lines.size() - 1) {
                    combined.append("\n");
                }
            }
            return SERIALIZER.deserialize(combined.toString());
        } else if (value instanceof String) {
            return SERIALIZER.deserialize((String) value);
        }

        return Component.text("Missing message: " + path);
    }

    public Component getMessage(String path, Map<String, String> placeholders) {
        Object value = getFromPath(messages, path);
        String text;

        if (value instanceof List) {
            List<?> lines = (List<?>) value;
            StringBuilder combined = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                combined.append(lines.get(i).toString());
                if (i < lines.size() - 1) {
                    combined.append("\n");
                }
            }
            text = combined.toString();
        } else if (value instanceof String) {
            text = (String) value;
        } else {
            return Component.text("Missing message: " + path);
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return SERIALIZER.deserialize(text);
    }

    public String getString(String path, String def) {
        Object value = getFromPath(config, path);
        return value != null ? value.toString() : def;
    }

    public String getString(String path) {
        return getString(path, "");
    }

    public boolean getBoolean(String path, boolean def) {
        Object value = getFromPath(config, path);
        return value instanceof Boolean ? (Boolean) value : def;
    }

    public int getInt(String path, int def) {
        Object value = getFromPath(config, path);
        return value instanceof Number ? ((Number) value).intValue() : def;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object value = getFromPath(config, path);
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object obj : (List<?>) value) {
                result.add(obj.toString());
            }
            return result;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public void set(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<String, Object>();
                current.put(parts[i], next);
            }
            current = (Map<String, Object>) next;
        }

        current.put(parts[parts.length - 1], value);
    }

    public void saveConfig() {
        try {
            Yaml yaml = new Yaml();
            FileWriter writer = new FileWriter(dataDirectory.resolve("config.yml").toFile());
            yaml.dump(config, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getAnuncios() {
        return anuncios;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public Map<String, Object> getMessages() {
        return messages;
    }

    private Object getFromPath(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        Object current = map;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }
}
