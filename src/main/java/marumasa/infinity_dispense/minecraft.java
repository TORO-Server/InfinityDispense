package marumasa.infinity_dispense;

import org.bukkit.plugin.java.JavaPlugin;

public final class minecraft extends JavaPlugin {

    @Override
    public void onEnable() {
        Config config = new Config(this);
        getServer().getPluginManager().registerEvents(new Events(this, config), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
