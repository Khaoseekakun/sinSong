package org.sinSong;

import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

public final class SinSong extends JavaPlugin {

    private String folder;
    private List<String> songList = new ArrayList<>();
    private Map<String, Integer> songDurations = new HashMap<>();
    private Map<String, String> songTitles = new HashMap<>();
    private FileConfiguration messages;
    private final Set<UUID> mutedPlayers = new HashSet<>();
    private int currentSongIndex = 0;
    private BukkitTask autoPlayTask;
    private boolean shuffleMode;
    private String autoPlaySong;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();

        FileConfiguration config = getConfig();
        folder = config.getString("resource_pack_folder", "custom");
        shuffleMode = config.getBoolean("shuffle", false);
        autoPlaySong = config.getString("auto_play");

        // Load songs and durations
        List<Map<?, ?>> rawSongs = config.getMapList("songs");
        for (Map<?, ?> songMap : rawSongs) {
            String name = String.valueOf(songMap.get("name"));
            String title = String.valueOf(songMap.get("title"));
            int duration = Integer.parseInt(String.valueOf(songMap.get("duration")));
            songList.add(name);
            songDurations.put(name, duration);
            songTitles.put(name, title);
        }

        if (autoPlaySong != null && !autoPlaySong.isEmpty()) {
            currentSongIndex = songList.indexOf(autoPlaySong);
            if (currentSongIndex == -1) currentSongIndex = 0;

            Bukkit.getScheduler().runTaskLater(this, () -> {
                startAutoPlayLoop();
            }, 60L);
        }
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String msg(String key, String... replacements) {
        String raw = messages.getString(key, "&cMessage not found: " + key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', messages.getString("prefix", "") + raw);
    }

    private void playToAll(String soundKey, String songTitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!mutedPlayers.contains(player.getUniqueId())) {
                player.stopSound(SoundCategory.RECORDS);
                player.playSound(player.getLocation(), soundKey, SoundCategory.RECORDS, 1.0f, 1.0f);
                player.sendMessage(msg("playing_song", "title", songTitle));
            }
        }
    }

    private void startAutoPlayLoop() {
        if (songList.isEmpty()) return;

        String songName = songList.get(currentSongIndex);
        String songTitle = songTitles.get(songName);
        String soundKey = folder + "." + songName;

        playToAll(soundKey, songTitle);

        int duration = songDurations.getOrDefault(songName, 20);
        autoPlayTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            if (shuffleMode) {
                currentSongIndex = new Random().nextInt(songList.size());
            } else {
                currentSongIndex = (currentSongIndex + 1) % songList.size();
            }
            startAutoPlayLoop();
        }, duration * 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle 'playsong' command
        if (command.getName().equalsIgnoreCase("sinsong")) {
            // Check if there are arguments (subcommands)
            if (args.length == 0) {
                sender.sendMessage(msg("main_command_usage"));
                return true;
            }

            // Handle 'reload' subcommand
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("sinsong.reload")) {
                    sender.sendMessage(msg("no_permission"));
                    return true;
                }

                reloadConfig();
                loadMessages();  // If you are reloading messages, make sure to call this method
                sender.sendMessage(msg("config_reloaded"));
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("playsong")) {
            if (!sender.hasPermission("sinsong.play")) {
                sender.sendMessage(msg("no_permission"));
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(msg("command_usage"));
                return true;
            }

            String songName = args[0];
            String songTitle = songTitles.get(songName);
            if (!songList.contains(songName)) {
                sender.sendMessage(msg("song_not_found", "title", songTitle));
                return true;
            }

            currentSongIndex = songList.indexOf(songName);
            playToAll(folder + "." + songName, songTitle);
            return true;
        }

        // Handle 'stopsong' command
        if (command.getName().equalsIgnoreCase("stopsong")) {
            if (!sender.hasPermission("sinsong.stop")) {
                sender.sendMessage(msg("no_permission"));
                return true;
            }

            if (autoPlayTask != null) autoPlayTask.cancel();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.stopSound(SoundCategory.MUSIC);
            }
            sender.sendMessage(msg("stopped_song"));
            return true;
        }

        // Handle 'skipsong' command
        if (command.getName().equalsIgnoreCase("skipsong")) {
            if (!sender.hasPermission("sinsong.skip")) {
                sender.sendMessage(msg("no_permission"));
                return true;
            }

            if (shuffleMode) {
                currentSongIndex = new Random().nextInt(songList.size());
            } else {
                currentSongIndex = (currentSongIndex + 1) % songList.size();
            }

            if (autoPlayTask != null) autoPlayTask.cancel();
            startAutoPlayLoop();
            return true;
        }

        // Handle 'togglemusic' command
        if (command.getName().equalsIgnoreCase("togglemusic")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if (mutedPlayers.contains(uuid)) {
                mutedPlayers.remove(uuid);
                player.sendMessage(msg("unmuted"));
            } else {
                mutedPlayers.add(uuid);
                player.sendMessage(msg("muted"));
            }
            return true;
        }

        // Handle 'reload' command
        return false;
    }

    @Override
    public void onDisable() {
        if (autoPlayTask != null) autoPlayTask.cancel();
    }
}