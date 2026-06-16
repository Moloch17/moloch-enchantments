package com.moloch.molochenchantments;

import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;

public final class AnvilListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onAnvilDamage(AnvilDamagedEvent event) {
        // Cancel 3 out of 4 damage events, giving anvils 4x normal durability.
        if (random.nextInt(4) != 0) {
            event.setCancelled(true);
        }
    }
}
