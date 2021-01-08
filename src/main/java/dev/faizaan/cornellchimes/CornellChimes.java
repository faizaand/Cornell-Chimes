package dev.faizaan.cornellchimes;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.Set;

public final class CornellChimes extends ExtendedJavaPlugin {

    private static final String LOG_PREFIX = "&8[&cCornell&fChimes&8]&r ";
    public static final String MESSAGE_PREFIX = ChatColor.translateAlternateColorCodes('&', "&f[&3&l✦&f]&r ");
    AudioManager audioManager;
    int frequencyMins;
    World cycleWorld;

    @Override
    public void enable() {
        this.audioManager = new AudioManager();
        loadConfig(true);
        new ChimesCommand().create(this);

        this.audioManager.cycleTracks(cycleWorld, frequencyMins);

        log("Enabled. Bingalee dingalee shall sound.");
    }

    @Override
    public void disable() {
        audioManager.cleanUp();
        log("Bye :)");
    }

    private void loadConfig(boolean firstTime) {
        if(firstTime) this.saveDefaultConfig();
        this.frequencyMins = getConfig().getInt("frequency");

        String cycleWorldName = Objects.requireNonNull(getConfig().getString("world"));
        this.cycleWorld = Bukkit.getWorld(cycleWorldName);
        if(this.cycleWorld == null) {
            this.cycleWorld = Bukkit.getWorlds().get(0);
            log("&cNo world found by name " + cycleWorldName + ". Defaulting to " + this.cycleWorld.getName() + ".");
        }

        ConfigurationSection trackSection = getConfig().getConfigurationSection("tracks");
        if(trackSection == null) {
            log("&cYou must specify at least one track in the configuration file.");
            log("&cAborting...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Set<String> tracks = trackSection.getKeys(true);
        for(String track : tracks) {
            String friendlyName = trackSection.getString("tracks." + track + ".friendlyName");
            String record = trackSection.getString("tracks." + track + ".record");
            if(friendlyName == null || record == null) {
                log("Failed to load track " + track + " because the friendly name or record were missing.");
                return;
            }
            try {
                audioManager.registerTrack(track, friendlyName, record);
            } catch(IllegalArgumentException e) {
                log("Failed to load track " + track + " because there is no such record as " + record + ".");
            }
        }
    }

    /**
     * Reloads the configuration and audio tracks anew.
     * Also restarts the cycle.
     */
    public void reloadConfig(Runnable callback) {
        super.reloadConfig();
        audioManager.cleanUp();
        loadConfig(false);
        callback.run();
    }

    public static void log(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', LOG_PREFIX + " " + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }
}
