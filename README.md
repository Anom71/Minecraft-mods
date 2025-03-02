# Minecraft Server Mod – Auto Update Utility

This project is a Minecraft server mod designed to automatically fetch the latest versions of mods using the Modrinth API. By utilizing each mod's file hash, the mod checks and retrieves the most recent version available. For modpack creators who manage hundreds of mods, updating each mod manually can be extremely time-consuming—this mod is developed to solve that problem.

## Project Background

Creating a modpack often involves managing hundreds of mods. Manually downloading and updating each mod to its latest version can be a tedious and error-prone task. This mod streamlines the process by automatically querying the Modrinth API with the mod file's hash value, thereby saving valuable time and effort for modpack creators.

## Features

- **Automatic Detection:** Automatically calculates the hash value of mod files to identify their current version.
- **Quick Updates:** Uses the Modrinth API to swiftly retrieve the latest version information.
- **Easy Integration:** Designed for seamless integration into existing modpack workflows.
- **Time Saver:** Eliminates the need for manual downloads, significantly reducing the update process time.

## Installation

1. **Requirements:**
   - A compatible Minecraft server (see project compatibility details for supported versions).
   - A Java Runtime Environment (version requirements may vary based on your server setup).
   - An internet connection to access the Modrinth API.

2. **Download and Setup:**
   - Download the latest `.jar` file from [GitHub Releases](https://github.com/your-repo-link/releases).
   - Place the downloaded `.jar` file into your server’s `mods` folder.
   - Start your Minecraft server—the mod will load automatically and begin checking for updates.

## Configuration

Configuration is typically managed via a file located in the `config/` directory, named `modupdater.yml` (or `.json`, depending on your implementation). Key configuration options include:

- **modrinth_api_url:** The URL for the Modrinth API (defaults to the official API endpoint).
- **hash_method:** The method used to calculate the mod file hash (e.g., SHA-1, MD5).
- **update_interval_minutes:** The interval (in minutes) for checking updates.
- **debug:** Toggle debug mode for detailed logging.

Example configuration (YAML):

```yaml
modrinth_api_url: "https://api.modrinth.com/v2/mod"
hash_method: "SHA-1"
update_interval_minutes: 60
debug: false
