package dev.faizaan.cornellchimes;

import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps track of available audio tracks, and plays them for players.
 * Also manages the automatic play every hour on the hour.
 */
public class AudioManager {

    private final Map<String, Track> tracks;
    private final LinkedList<Track> trackList;
    private final Set<UUID> acceptedPlayers;
    private final AtomicInteger cyclePosition = new AtomicInteger(0);
    private Task autoPlayer = null;

    public AudioManager() {
        this.tracks = new HashMap<>();
        this.trackList = new LinkedList<>();
        this.acceptedPlayers = new HashSet<>();
    }

    /**
     * Adds a track to the audio manager.
     *
     * @param id           the ID of the track, which should contain no spaces
     * @param friendlyName Friendly name, shown to users when the song plays.
     * @param record       The record's actual name. not case sensitive.
     * @throws IllegalArgumentException if the record is invalid.
     */
    public void registerTrack(String id, String friendlyName, String record) {
        // this will throw an illegal argument exception if the record is invalid.
        Material recordMat = Material.valueOf("MUSIC_DISC_" + record.toUpperCase());
        Track value = new Track(id, friendlyName, recordMat);
        tracks.put(friendlyName, value);
        trackList.add(value);
    }

    /**
     * Play a track to a particular player in the game.
     * This will notify the player that the track is playing.
     *
     * @param track  The track. Must not be null.
     * @param target The player. Must not be null.
     */
    public void playTrack(Track track, Player target) {
        Validate.notNull(track);
        Validate.notNull(target);
        if(!this.acceptedPlayers.contains(target.getUniqueId())) {
            target.sendMessage(CornellChimes.MESSAGE_PREFIX + ChatColor.RED + "You can't do this because you did not accept the Chimes resource pack.");
            target.sendMessage(CornellChimes.MESSAGE_PREFIX + "Log out and log back in, and accept the resource pack, in order to hear the Chimes.");
            return;
        }
        target.playEffect(target.getLocation(), Effect.RECORD_PLAY, track.getRecord());
        target.sendMessage(
                CornellChimes.MESSAGE_PREFIX + ChatColor.ITALIC + "Now playing " + ChatColor.RESET
                        + ChatColor.DARK_AQUA + track.getFriendlyName() + ChatColor.WHITE + ". Turn up your sound!");
    }

    /**
     * Broadcast a track to the entire world.
     * If a new player joins, they will not hear the track until it's played again.
     *
     * @param track The track. Must not be null.
     * @param world The world. Must not be null.
     */
    public void broadcastTrack(Track track, World world) {
        Validate.notNull(track);
        Validate.notNull(world);
        world.getPlayers().stream()
                .filter(p -> this.acceptedPlayers.contains(p.getUniqueId()))
                .forEach(p -> playTrack(track, p));
    }

    /**
     * Mark a player as eligible to hear the chimes, which usually happens once they
     * accept the Chimes resource pack, which contains the audio files.
     *
     * @param player The player. Must not be null.
     */
    public void acceptPlayer(Player player) {
        Validate.notNull(player);
        this.acceptedPlayers.add(player.getUniqueId());
    }

    /**
     * Starts cycling through each track indefinitely, playing one per every number of minutes specified.
     * The trask will be canceled when the AudioManager is cleaned up. The first play will be 5 minutes
     * after this method is called.
     *
     * @param cycleWorld       the world to play the tracks in.
     * @param frequencyMinutes the number of minutes to wait between each song.
     * @see AudioManager#cleanUp()
     */
    public void cycleTracks(World cycleWorld, int frequencyMinutes) {
        if (this.autoPlayer != null) return;
        this.autoPlayer = Schedulers.builder()
                .sync()
                .after(5, TimeUnit.MINUTES)
                .every(frequencyMinutes, TimeUnit.MINUTES)
                .run(() -> {
                    int trackNum = cyclePosition.getAndIncrement();
                    if (trackNum + 1 == tracks.size()) {
                        cyclePosition.set(0);
                    }
                    broadcastTrack(trackList.get(trackNum), cycleWorld);
                });
    }

    /**
     * Stops the track cycle if it's playing, and clears the track lists.
     */
    public void cleanUp() {
        if (this.autoPlayer != null) {
            this.autoPlayer.close();
            this.autoPlayer = null;
        }
        tracks.clear();
        trackList.clear();
    }

    public Optional<Track> getTrackByFriendlyName(String friendlyName) {
        return Optional.ofNullable(tracks.get(friendlyName));
    }

    public Set<String> getAllTrackFriendlyNames() {
        return tracks.keySet();
    }

}
