package com.moloch.molochenchantments;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Floats an eye of ender above every enchanting table. The eye is invisible
 * until a player comes within range, then fades in (by scaling up) and faces the
 * player; subtle End particles swirl around the table whenever a player is near.
 * The displays are non-persistent and are (re)spawned per chunk so they never
 * accumulate or survive a restart.
 */
public final class EnchantTableDecorator implements Listener {

    private static final String EYE_TAG = "moloch_scrying_eye";
    private static final float HOVER_HEIGHT = 1.30f;
    private static final float EYE_SCALE = 0.45f;

    private static final long INTERVAL = 5L;        // ticks between distance checks
    private static final int FADE_TICKS = 8;        // interpolation length of the fade

    // Distance (squared) from the table CENTER at which the eye shows, matched to
    // the vanilla book which opens within 3 blocks of the block center
    // (getNearestPlayer(center, 3.0)). No hysteresis, so the eye fades exactly when
    // the book closes.
    private static final double SHOW_RANGE_SQ = 3.0 * 3.0;

    // Swirl: PORTAL particles arcing inward, uniformly distributed on a sphere around the eye.
    private static final int SWIRL_COUNT = 1;          // particles per emission
    private static final double SWIRL_RADIUS = 0.6;    // radius of the surrounding sphere

    private final Plugin plugin;
    private final Map<String, UUID> eyes = new HashMap<>();
    private final Map<UUID, Boolean> visible = new HashMap<>();
    private final Map<UUID, Integer> shownTicks = new HashMap<>();
    private BukkitTask task;

    public EnchantTableDecorator(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                decorateChunk(chunk);
            }
        }
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, INTERVAL);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                removeTaggedEyes(chunk);
            }
        }
        eyes.clear();
        visible.clear();
        shownTicks.clear();
    }

    // ---- Chunk lifecycle ----

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        decorateChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Non-persistent displays vanish with the chunk; just drop our tracking.
        dropChunkTracking(event.getChunk());
    }

    private void dropChunkTracking(Chunk chunk) {
        String prefix = chunk.getWorld().getUID() + ":" + chunk.getX() + ":" + chunk.getZ() + ":";
        eyes.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(prefix)) {
                visible.remove(entry.getValue());
                shownTicks.remove(entry.getValue());
                return true;
            }
            return false;
        });
    }

    private void decorateChunk(Chunk chunk) {
        // Clear any stale displays (e.g. left by a hard crash) and tracking, then rebuild.
        dropChunkTracking(chunk);
        removeTaggedEyes(chunk);
        for (BlockState state : chunk.getTileEntities()) {
            if (state.getType() == Material.ENCHANTING_TABLE) {
                spawnEye(state.getBlock());
            }
        }
    }

    private void removeTaggedEyes(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ItemDisplay && entity.getScoreboardTags().contains(EYE_TAG)) {
                entity.remove();
            }
        }
    }

    // ---- Block lifecycle ----

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.ENCHANTING_TABLE) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (block.getType() == Material.ENCHANTING_TABLE) spawnEye(block);
        });
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.ENCHANTING_TABLE) return;
        removeEye(event.getBlock());
    }

    /** The hover point of the floating eye above a table block (whether or not it is visible). */
    public Location eyeLocation(Location tableBlock) {
        return tableBlock.clone().add(0.5, HOVER_HEIGHT, 0.5);
    }

    // ---- Display management ----

    private void spawnEye(Block block) {
        String key = keyFor(block);
        if (eyes.containsKey(key)) return;

        Location loc = block.getLocation().add(0.5, HOVER_HEIGHT, 0.5);
        ItemDisplay display = block.getWorld().spawn(loc, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(Material.ENDER_EYE));
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            // Always face the player; the fade is done by scaling, not rotation.
            d.setBillboard(Display.Billboard.CENTER);
            d.setBrightness(new Display.Brightness(15, 15));
            d.setViewRange(1.0f);
            d.setPersistent(false);
            d.addScoreboardTag(EYE_TAG);
            d.setInterpolationDelay(0);
            d.setInterpolationDuration(FADE_TICKS);
            // Start invisible (scaled to zero).
            d.setTransformation(transformScaled(0f));
        });
        visible.put(display.getUniqueId(), false);
        eyes.put(key, display.getUniqueId());
    }

    private void removeEye(Block block) {
        UUID id = eyes.remove(keyFor(block));
        if (id == null) return;
        visible.remove(id);
        shownTicks.remove(id);
        Entity entity = plugin.getServer().getEntity(id);
        if (entity != null) entity.remove();
    }

    private void tick() {
        for (UUID id : eyes.values()) {
            if (!(plugin.getServer().getEntity(id) instanceof ItemDisplay display)) continue;

            Location eye = display.getLocation();
            // Measure from the table CENTER (not the floating eye) so the fade
            // coincides with the vanilla book opening/closing.
            Location center = eye.clone().subtract(0, HOVER_HEIGHT - 0.5, 0);
            double nearestSq = nearestPlayerSq(center);

            boolean show = nearestSq < SHOW_RANGE_SQ;
            boolean wasVisible = visible.getOrDefault(id, false);
            if (show != wasVisible) {
                display.setInterpolationDelay(0);
                display.setInterpolationDuration(FADE_TICKS);
                display.setTransformation(transformScaled(show ? EYE_SCALE : 0f));
                visible.put(id, show);
                shownTicks.put(id, 0);
            } else if (show) {
                shownTicks.put(id, shownTicks.getOrDefault(id, 0) + (int) INTERVAL);
            }

            // Particles only while the eye is fully materialised: start once the
            // fade-in has finished, and stop the instant it begins fading out.
            if (show && shownTicks.getOrDefault(id, 0) >= FADE_TICKS) {
                swirl(eye);
            }
        }
    }

    private double nearestPlayerSq(Location loc) {
        double nearestSq = Double.MAX_VALUE;
        for (Player player : loc.getWorld().getPlayers()) {
            nearestSq = Math.min(nearestSq, player.getLocation().distanceSquared(loc));
        }
        return nearestSq;
    }

    /**
     * Particles arc inward toward the eye from a uniform sphere around it. count=0 is a
     * directional spawn whose offset is the velocity (scaled by the trailing speed): the particle
     * appears at {@code eye + radius*dir} and converges back to the eye. Directions are a
     * uniformly random point on the unit sphere.
     */
    private void swirl(Location eye) {
        World world = eye.getWorld();
        for (int i = 0; i < SWIRL_COUNT; i++) {
            double theta = Math.random() * Math.PI * 2.0;
            double uy = Math.random() * 2.0 - 1.0;      // uniform cos(phi) for an even sphere
            double ring = Math.sqrt(1.0 - uy * uy);
            double ux = ring * Math.cos(theta);
            double uz = ring * Math.sin(theta);
            world.spawnParticle(Particle.PORTAL, eye, 0, ux, uy, uz, SWIRL_RADIUS);
        }
    }

    private Transformation transformScaled(float scale) {
        return new Transformation(
            new Vector3f(0f, 0f, 0f),
            new Quaternionf(),
            new Vector3f(scale, scale, scale),
            new Quaternionf()
        );
    }

    private String keyFor(Block block) {
        return block.getWorld().getUID() + ":"
            + (block.getX() >> 4) + ":" + (block.getZ() >> 4) + ":"
            + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }
}
