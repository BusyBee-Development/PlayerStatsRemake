package com.fernsehheft.playerstatsremake.core.utils;

import com.fernsehheft.playerstatsremake.core.config.ConfigHandler;
import com.fernsehheft.playerstatsremake.core.msg.OutputManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks Modrinth for newer plugin releases and notifies the console and server operators.
 */
public final class ModrinthUpdateChecker {

  /** Modrinth project slug or UUID — update when the Modrinth listing is published. */
  public static final String MODRINTH_PROJECT_ID = "playerstatsremake";

  private static final String VERSIONS_URL =
      "https://api.modrinth.com/v2/project/" + MODRINTH_PROJECT_ID + "/version";
  private static final String PROJECT_PAGE_URL =
      "https://modrinth.com/plugin/" + MODRINTH_PROJECT_ID;
  private static final Pattern VERSION_NUMBER_PATTERN =
      Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");

  private static volatile ModrinthUpdateChecker instance;

  private final JavaPlugin plugin;
  private final ConfigHandler config;
  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
    Thread thread = new Thread(r, "PlayerStats-UpdateCheck");
    thread.setDaemon(true);
    return thread;
  });

  private volatile boolean updateAvailable;
  private volatile @Nullable String latestVersion;
  private volatile @Nullable String downloadUrl = PROJECT_PAGE_URL;

  private ModrinthUpdateChecker(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
    this.config = ConfigHandler.getInstance();
  }

  public static @NotNull ModrinthUpdateChecker getInstance(@NotNull JavaPlugin plugin) {
    ModrinthUpdateChecker local = instance;
    if (local != null) {
      return local;
    }
    synchronized (ModrinthUpdateChecker.class) {
      if (instance == null) {
        instance = new ModrinthUpdateChecker(plugin);
      }
      return instance;
    }
  }

  public void scheduleCheck() {
    if (!config.checkForUpdates()) {
      return;
    }
    Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> executor.execute(this::checkForUpdateAsync), 200L);
  }

  public boolean isUpdateAvailable() {
    return updateAvailable;
  }

  public @Nullable String getLatestVersion() {
    return latestVersion;
  }

  public @NotNull String getDownloadUrl() {
    return downloadUrl != null ? downloadUrl : PROJECT_PAGE_URL;
  }

  public void notifyOpIfNeeded(@NotNull Player player) {
    if (!updateAvailable || !config.notifyOpsAboutUpdates() || !player.isOp()) {
      return;
    }
    String remote = latestVersion;
    if (remote == null) {
      return;
    }
    String local = plugin.getPluginMeta().getVersion();
    Component message = OutputManager.getInstance()
        .updateAvailableMessage(local, remote, getDownloadUrl());
    Bukkit.getGlobalRegionScheduler().run(plugin, task -> player.sendMessage(message));
  }

  private void checkForUpdateAsync() {
    try {
      HttpClient client = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(10))
          .build();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(VERSIONS_URL))
          .header("User-Agent", "PlayerStatsRemake/" + plugin.getPluginMeta().getVersion())
          .timeout(Duration.ofSeconds(15))
          .GET()
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        logCheckFailure("Modrinth API returned HTTP " + response.statusCode());
        return;
      }

      String remoteVersion = parseLatestReleaseVersion(response.body());
      if (remoteVersion == null) {
        logCheckFailure("Could not parse latest version from Modrinth response");
        return;
      }

      String localVersion = plugin.getPluginMeta().getVersion();
      if (!VersionComparator.isNewer(remoteVersion, localVersion)) {
        updateAvailable = false;
        return;
      }

      latestVersion = remoteVersion;
      updateAvailable = true;
      downloadUrl = PROJECT_PAGE_URL;

      Bukkit.getGlobalRegionScheduler().run(plugin, task ->
          plugin.getLogger().info(
              "A new version of PlayerStatsRemake is available: v" + remoteVersion
                  + " (you are running v" + localVersion + "). Download: " + getDownloadUrl()));
    } catch (Exception e) {
      logCheckFailure(e.getMessage());
    }
  }

  private @Nullable String parseLatestReleaseVersion(@NotNull String json) {
    int searchFrom = 0;
    while (searchFrom < json.length()) {
      int typeIndex = json.indexOf("\"version_type\"", searchFrom);
      if (typeIndex < 0) {
        break;
      }
      String type = extractQuotedJsonValue(json, typeIndex + "\"version_type\"".length());
      int versionIndex = json.indexOf("\"version_number\"", typeIndex);
      if (versionIndex < 0) {
        break;
      }
      String version = extractQuotedJsonValue(json, versionIndex + "\"version_number\"".length());
      if (version != null && "release".equalsIgnoreCase(type)) {
        return version;
      }
      searchFrom = typeIndex + 1;
    }

    Matcher fallback = VERSION_NUMBER_PATTERN.matcher(json);
    if (fallback.find()) {
      return fallback.group(1);
    }
    return null;
  }

  private @Nullable String extractQuotedJsonValue(@NotNull String json, int fromIndex) {
    int colon = json.indexOf(':', fromIndex);
    if (colon < 0) {
      return null;
    }
    int startQuote = json.indexOf('"', colon + 1);
    if (startQuote < 0) {
      return null;
    }
    int endQuote = json.indexOf('"', startQuote + 1);
    if (endQuote < 0) {
      return null;
    }
    return json.substring(startQuote + 1, endQuote);
  }

  private void logCheckFailure(@Nullable String detail) {
    if (config.getDebugLevel() >= 2 && detail != null) {
      MyLogger.logLowLevelMsg("Update check skipped: " + detail);
    }
  }
}
