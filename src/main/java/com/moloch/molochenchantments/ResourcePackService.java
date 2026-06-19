package com.moloch.molochenchantments;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.UUID;

/**
 * Points clients at the externally hosted resource pack. The pack is fetched
 * once at startup so its SHA-1 can be computed automatically (clients need the
 * hash for integrity checks and caching); delivery is sent on join.
 */
public final class ResourcePackService implements Listener {

    private final Plugin plugin;

    private ResourcePackRequest request;
    private volatile boolean ready;

    public ResourcePackService(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean start() {
        if (!plugin.getConfig().getBoolean("resource-pack.enabled", true)) {
            plugin.getLogger().info("Resource pack delivery disabled in config.");
            return false;
        }

        String url = plugin.getConfig().getString("resource-pack.url", "");
        if (url.isBlank()) {
            plugin.getLogger().warning("resource-pack.url is empty; resource pack will not be sent.");
            return false;
        }

        boolean required = plugin.getConfig().getBoolean("resource-pack.required", true);
        String prompt = plugin.getConfig().getString("resource-pack.prompt", "");

        // Fetch + hash off the main thread so startup never blocks on the network.
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String hash = "";
            try {
                hash = sha1Hex(download(url));
            } catch (Exception e) {
                plugin.getLogger().warning("Could not fetch pack from " + url + " to compute its hash; sending"
                    + " without an integrity hash (clients will re-download each time): " + e.getMessage());
            }

            UUID packId = UUID.nameUUIDFromBytes(url.getBytes());
            request = ResourcePackRequest.resourcePackRequest()
                .packs(ResourcePackInfo.resourcePackInfo(packId, URI.create(url), hash))
                .required(required)
                .prompt(Component.text(prompt))
                .replace(true)
                .build();
            ready = true;
            plugin.getLogger().info("Resource pack ready: " + url + (hash.isEmpty() ? " (no hash)" : " (sha1=" + hash + ")"));

            // Re-send to anyone already online (e.g. after a /reload).
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendResourcePacks(request);
                }
            });
        });

        return true;
    }

    public void stop() {
        ready = false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (ready) {
            event.getPlayer().sendResourcePacks(request);
        }
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED ->
                plugin.getLogger().info(event.getPlayer().getName() + " loaded the resource pack.");
            case DECLINED ->
                plugin.getLogger().info(event.getPlayer().getName() + " declined the resource pack.");
            case FAILED_DOWNLOAD ->
                plugin.getLogger().warning(event.getPlayer().getName() + " FAILED to download the pack — the URL is"
                    + " unreachable from their client or the sha1 mismatched. Verify resource-pack.url is public.");
            case INVALID_URL ->
                plugin.getLogger().warning(event.getPlayer().getName() + " got an INVALID pack URL.");
            case FAILED_RELOAD ->
                plugin.getLogger().warning(event.getPlayer().getName() + " downloaded the pack but FAILED to apply it"
                    + " — usually a wrong pack_format in pack.mcmeta for this client version.");
            default -> { /* ACCEPTED, DOWNLOADED, DISCARDED: transient, no log */ }
        }
    }

    private static byte[] download(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build();
        HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + resp.statusCode());
        }
        return resp.body();
    }

    private static String sha1Hex(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }
}
