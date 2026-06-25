package net.joseplay.jpDeathChest.entities;

import net.joseplay.allianceutils.Allianceutils;
import net.joseplay.jpDeathChest.JpDeathChest;
import net.joseplay.jpDeathChest.utils.ParticleUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathChest {
    private final UUID ownerUUID;
    private final UUID chestUUID;
    private final Map<Integer, ItemStack> mapItems;
    private final Location chestLocation;
    private int time = 600;
    private BukkitTask task;
    private final List<ArmorStand> holograms = new ArrayList<>();
    private BlockState originalState = null;
    private boolean open = false;
    private List<String> holos = List.of(
            "§eTempo: {time}",
            "§b{player}",
            "§cBaú da Morte"
    );


    public DeathChest(UUID chestUUID, int time, UUID ownerUUID, Map<Integer, ItemStack> mapItems, Location chestLocation) {
        if (chestUUID != null) {
            this.chestUUID = chestUUID;
        } else {
            this.chestUUID = UUID.randomUUID();
        }

        if (time > 0) this.time = time;
        this.ownerUUID = ownerUUID;
        this.mapItems = mapItems;
        this.chestLocation = findSegureLocation(chestLocation);
    }

    public DeathChest(UUID ownerUUID, Map<Integer, ItemStack> mapItems, Location chestLocation) {
        this(null, 0, ownerUUID, mapItems, chestLocation);
    }


    public void spawn(){
        originalState = chestLocation.getBlock().getState();
        chestLocation.getBlock().setType(Material.CHEST);
        chestLocation.getBlock().setMetadata("jpDeathChest", new FixedMetadataValue(Allianceutils.getPlugin(), chestUUID.toString()));

        createHolograms();
        startTime();
    }


    private void startTime(){

        Player player = Bukkit.getPlayer(ownerUUID);
        BukkitRunnable dTask = new BukkitRunnable() {
            int currentTick = 0;
            double y = 0;
            double radius = 0.5;
            int points = 20;
            @Override
            public void run() {
                if (time <= 0){
                    JpDeathChest.getInstance().getDeathChestManager().removeDeathChest(DeathChest.this, true);
                    return;
                }


                if (currentTick >= 20) {
                    currentTick = 0;
                    updateHolograms();
                    time--;
                }

                Location loc = chestLocation.clone().add(0.5, 0.5, 0.5);
                ParticleUtils.spawnBeam(loc, Particle.END_ROD, 10);

                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI / i * points + currentTick;

                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location loc1 = loc.clone().add(x, y, z);
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc1, 1, 0,0,0,0);
                    y += 0.1;
                }

                currentTick += 5;
                if (y >= 10) y = 0;
            }
        };

        task = JpDeathChest.getInstance().getTaskManager()
                .runTaskTimer(dTask, 0, 5);

    }

    public void stopTime(boolean drop){

        if (task != null && !task.isCancelled()){
            task.cancel();
            task = null;
        }

        synchronized (holograms){
            for (ArmorStand holo : holograms){
                try {
                    holo.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        if (drop){

            for (ItemStack item : mapItems.values()){
                chestLocation.getWorld().dropItem(chestLocation, item);
            }

            mapItems.clear();
        }


        if (originalState != null){
            originalState.update(true);
        }

        open = true;
    }

    private void updateHolograms(){
        Player player = Bukkit.getPlayer(ownerUUID);
       for (int i = 0; i < holos.size(); i++){
           ArmorStand stand = holograms.get(i);
           String string = holos.get(i);

           if (!string.isEmpty() && stand != null) {

               string = string
                       .replace("{player}", Bukkit.getOfflinePlayer(ownerUUID).getName())
                       .replace("{time}", parseTime(time));


               stand.setCustomName(string);
           }
       }
    }

    private void createHolograms(){

        double offSet = 0.30;
        for (int i = 0; i < holos.size(); i++){
            ArmorStand armorStand = chestLocation.getWorld().spawn(chestLocation.getBlock().getLocation().clone().add(0.5, 0.5 + offSet, 0.5), ArmorStand.class);
            armorStand.setSmall(true);
            armorStand.setMarker(true);
            armorStand.setInvisible(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            holograms.add(armorStand);
            offSet += 0.30;
        }
    }

    private Location findSegureLocation(Location location){

        if (location.getBlock().getType().isAir()) return location.getBlock().getLocation();


        Block currentBlock = location.getBlock().getRelative(BlockFace.UP);
        int currentY = currentBlock.getY();
        while (!currentBlock.getType().isAir() && currentY <= location.getWorld().getLogicalHeight()){
            currentBlock = currentBlock.getRelative(BlockFace.UP);
            currentY++;
        }

        return currentBlock.getLocation();
    }




    private String parseTime(int time){
        int minutes = time / 60;
        int seconds = time % 60;

        return String.format("%s:%s", minutes, seconds);
    }

    private String parseLocation(){
        int x = chestLocation.getBlockX();
        int y = chestLocation.getBlockY();
        int z = chestLocation.getBlockZ();

        return x + ", " + y + ", " + z;
    }



    //Getters


    public int getTime() {
        return time;
    }

    public String getTimeParsed(){
        return parseTime(getTime());
    }

    public String getLocationParsed(){
        return parseLocation();
    }

    public boolean isOpen() {
        return open;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public UUID getChestUUID() {
        return chestUUID;
    }

    public Map<Integer, ItemStack> getMapItems() {
        return mapItems;
    }

    public Location getChestLocation() {
        return chestLocation;
    }
}
