package dev.faizaan.cornellchimes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class CornellChimes extends JavaPlugin {

    private static final String LOG_PREFIX = "&8[&cCornell&fChimes&8]&r ";

    @Override
    public void onEnable() {
        log("Enabled. Bingalee dingalee shall sound.");
    }

    @Override
    public void onDisable() {
        log("Bye :)");
    }

    public static void log(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', LOG_PREFIX + " " + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }
}
