package net.joseplay.jpDeathChest.listeners;

import net.joseplay.jpDeathChest.JpDeathChest;
import net.joseplay.jpDeathChest.entities.DeathChest;
import net.joseplay.jpDeathChest.gui.DeathChestGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class DeathChestEvents implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event){
        if (event.getKeepInventory()) return;
        if (event.getDrops().isEmpty()) return;


        DeathChest deathChest = JpDeathChest.getInstance()
                .getDeathChestManager()
                .createDeathChest(event.getEntity());


        event.getDrops().clear();
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event){
        if (!event.hasBlock()) return;

        if (event.getClickedBlock().hasMetadata("jpDeathChest")){
            UUID uuid = UUID.fromString(event.getClickedBlock().getMetadata("jpDeathChest").getFirst().asString());

            DeathChest deathChest = JpDeathChest.getInstance().getDeathChestManager().getDeathChest(uuid);

            if (deathChest != null){
                event.setCancelled(true);
                new DeathChestGUI(deathChest).open(event.getPlayer());
            }
        }
    }

}
