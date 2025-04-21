# SinSong Plugin - Music for Minecraft

**SinSong** is a Minecraft plugin designed to enhance your gameplay experience by adding custom music tracks that can be played directly in the game. Whether you’re creating a custom atmosphere for your server or simply want to enjoy music while playing, SinSong provides an easy-to-use solution.

## Features
- **Custom Songs**: Easily add and play custom music tracks in Minecraft by linking them to your resource pack.
- **Auto-Play**: Automatically start playing a song as soon as the plugin is loaded.
- **Looping**: Songs can loop indefinitely or until manually stopped, providing an uninterrupted musical experience.
- **Shuffle Mode**: Play songs in random order for a more dynamic listening experience.
- **Player Control**: Players can mute or unmute music individually, so they can choose whether they want to hear the sounds.
- **Permissions-Based Commands**: Server administrators can control who has access to play, stop, or skip songs through permission-based commands.

## How It Works
- **Songs List**: Define your music tracks in the `config.yml` file, where each song has a name, title, and duration.
- **Resource Pack**: The plugin integrates seamlessly with Minecraft's resource pack system, so your music is played through the game's sound system.
- **Customizable**: The plugin allows you to configure various settings, such as which song to play automatically on startup and whether songs should be shuffled or played in a set order.

## Installation
1. Download the SinSong plugin JAR file.
2. Place the JAR file into the `plugins` folder of your Minecraft server.
3. Restart your server to load the plugin.

## Configuration
Customize your music playlist, resource pack location, and other settings by editing the `config.yml` file. You can define which songs are available, set durations, and choose whether to shuffle the playlist or play songs in a fixed order.

### Example `config.yml`
```yaml
resource_pack_folder: "custom"  # This matches your folder inside assets/minecraft/sounds/custom/

songs:
  - name: music_1
    title: "Song 1"
    duration: 15 # seconds
  - name: music_2
    title: "Song 2"
    duration: 20 # seconds

auto_play: "music_1"
shuffle: true # true or false
