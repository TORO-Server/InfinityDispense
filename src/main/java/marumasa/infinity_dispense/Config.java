package marumasa.infinity_dispense;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    public final String name;

    public Config(minecraft plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        name = config.getString("name");
    }
}