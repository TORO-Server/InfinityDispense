package marumasa.infinity_dispense;

import org.bukkit.plugin.java.JavaPlugin;

public final class minecraft extends JavaPlugin {

    @Override
    public void onEnable() {
        Config config = new Config(this);
        Marker marker = new Marker(this, config);
        getServer().getPluginManager().registerEvents(new Events(this, marker), this);
        getCommand("infinitydispense").setExecutor(new MarkCommand(marker));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
