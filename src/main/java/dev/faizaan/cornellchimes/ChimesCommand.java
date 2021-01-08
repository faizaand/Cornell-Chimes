package dev.faizaan.cornellchimes;

import me.lucko.helper.Commands;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ChimesCommand {

    public void create(CornellChimes plugin) {
        // handles /chimes, /chimes broadcast, and /chimes <chime>
        Commands.create()
                .assertPermission("chimes.play")
                .assertPlayer()
                .handler(c -> {
                    if(c.args().size() == 0) {
                        // just print the list
                        c.sender().sendMessage(CornellChimes.MESSAGE_PREFIX + ChatColor.GOLD + "Available tracks:");
                        plugin.audioManager.getAllTrackFriendlyNames().forEach(name -> sendClickableTrack(c.sender(), name));
                        c.sender().sendMessage(CornellChimes.MESSAGE_PREFIX + ChatColor.WHITE + "You can click any of the above tracks to play it!");
                        return;
                    }

                    String subcommand = c.args().get(0);
                    if(subcommand.equalsIgnoreCase("broadcast")) {
                        if(!c.sender().hasPermission("chimes.admin")) {
                            c.sender().sendMessage(ChatColor.RED + "You do not have permission to perform this command.");
                            return;
                        }

                        String trackFriendlyName = String.join(" ", c.args().subList(1, c.args().size()));
                        Optional<Track> trackOpt = plugin.audioManager.getTrackByFriendlyName(trackFriendlyName);
                        if(!trackOpt.isPresent()) {
                            c.sender().sendMessage(CornellChimes.MESSAGE_PREFIX + ChatColor.RED + "That track does not exist.");
                            c.sender().sendMessage(CornellChimes.MESSAGE_PREFIX + "Type " + ChatColor.DARK_AQUA + "/chimes" + ChatColor.RESET + " for a full list of available tracks.");
                            return;
                        }

                        plugin.audioManager.broadcastTrack(trackOpt.get(), c.sender().getWorld());
                    } else if(subcommand.equalsIgnoreCase("reload")) {
                        if(!c.sender().hasPermission("chimes.admin")) {
                            c.sender().sendMessage(ChatColor.RED + "You do not have permission to perform this command.");
                            return;
                        }

                        plugin.reloadConfig(() -> c.sender().sendMessage(CornellChimes.MESSAGE_PREFIX + "Cornell Chimes has been reloaded."));
                    } else {
                        String trackFriendlyName = String.join(" ", c.args());
                        Optional<Track> trackOpt = plugin.audioManager.getTrackByFriendlyName(trackFriendlyName);
                        if (!trackOpt.isPresent()) {
                            c.sender().sendMessage(CornellChimes.MESSAGE_PREFIX + ChatColor.RED + "That track does not exist.");
                            c.sender().sendMessage(CornellChimes.MESSAGE_PREFIX + "Type " + ChatColor.DARK_AQUA + "/chimes" + ChatColor.RESET + " for a full list of available tracks.");
                            return;
                        }

                        plugin.audioManager.playTrack(trackOpt.get(), c.sender());
                    }
                })
                .registerAndBind(plugin, "chimes");
    }

    private void sendClickableTrack(Player target, String trackFriendlyName) {
        TextComponent component = new TextComponent(ChatColor.GRAY + "- " + ChatColor.DARK_AQUA + trackFriendlyName);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chimes " + trackFriendlyName));
        target.spigot().sendMessage(component);
    }

}
