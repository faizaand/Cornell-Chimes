package dev.faizaan.cornellchimes;

import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

public final class CornellChimes extends ExtendedJavaPlugin {

    private static final String LOG_PREFIX = "&8[&cCornell&fChimes&8]&r ";
    public static final String MESSAGE_PREFIX = ChatColor.translateAlternateColorCodes('&', "&f[&3&l✦&f]&r ");
    AudioManager audioManager;
    int frequencyMins;
    int delayMins;
    World cycleWorld;
    String packUrl;
    byte[] packHash;

    @Override
    public void enable() {
        this.audioManager = new AudioManager();
        loadConfig(true);
        new ChimesCommand().create(this);
        this.enforceResourcePack();
        this.audioManager.cycleTracks(cycleWorld, frequencyMins, delayMins);

        log("Enabled. Bingalee dingalee shall sound.");
    }

    @Override
    public void disable() {
        audioManager.cleanUp();
        log("Bye :)");
    }

    private void loadConfig(boolean firstTime) {
        if (firstTime) this.saveDefaultConfig();
        this.frequencyMins = getConfig().getInt("frequency");
        this.delayMins = getConfig().getInt("delay");

        String cycleWorldName = Objects.requireNonNull(getConfig().getString("world"));
        this.cycleWorld = Bukkit.getWorld(cycleWorldName);
        if (this.cycleWorld == null) {
            this.cycleWorld = Bukkit.getWorlds().get(0);
            log("&cNo world found by name " + cycleWorldName + ". Defaulting to " + this.cycleWorld.getName() + ".");
        }

        this.packUrl = Objects.requireNonNull(getConfig().getString("pack.url"));
        this.packHash = DatatypeConverter.parseHexBinary(Objects.requireNonNull(getConfig().getString("pack.hash")));

        ConfigurationSection trackSection = getConfig().getConfigurationSection("tracks");
        if (trackSection == null) {
            log("&cYou must specify at least one track in the configuration file.");
            log("&cAborting...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Set<String> tracks = trackSection.getKeys(false);
        for (String track : tracks) {
            String friendlyName = getConfig().getString("tracks." + track + ".friendlyName");
            String record = getConfig().getString("tracks." + track + ".record");
            if (friendlyName == null || record == null) {
                log("Failed to load track " + track + " because the friendly name or record were missing.");
                continue;
            }
            try {
                audioManager.registerTrack(track, friendlyName, record);
            } catch (IllegalArgumentException e) {
                log("Failed to load track " + track + " because there is no such record as " + record + ".");
            }
        }
    }

    private void enforceResourcePack() {
        Events.subscribe(PlayerJoinEvent.class).handler(e -> e.getPlayer().setResourcePack(this.packUrl, this.packHash));
        Events.subscribe(PlayerQuitEvent.class).handler(e -> this.audioManager.clearPlayer(e.getPlayer()));
        Events.subscribe(PlayerResourcePackStatusEvent.class).handler(e -> {
            if(e.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) audioManager.acceptPlayer(e.getPlayer());
            else if(e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
                e.getPlayer().sendMessage(MESSAGE_PREFIX + ChatColor.RED + "Since you declined resource packs, you won't be able to hear the Cornell Chimes.");
                e.getPlayer().sendMessage(MESSAGE_PREFIX + "If you change your mind, follow this link to learn how to enable them: " + ChatColor.GOLD + "http://s.moep.tv/rp");
            }
        });
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
