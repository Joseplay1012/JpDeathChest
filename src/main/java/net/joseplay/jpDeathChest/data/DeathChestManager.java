package net.joseplay.jpDeathChest.data;

import net.joseplay.jpDeathChest.JpDeathChest;
import net.joseplay.jpDeathChest.entities.DeathChest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DeathChestManager {
    private final Map<UUID, List<UUID>> ownerChests = new ConcurrentHashMap<>();
    private final Map<UUID, DeathChest> deathChestMap = new ConcurrentHashMap<>();
    private final String DEATHCHEST_FILE = "chests.yml";


    public DeathChest getDeathChest(UUID uuid){
        return deathChestMap.get(uuid);
    }

    public List<DeathChest> getOwnerDeathChests(UUID uuid){
        return ownerChests.getOrDefault(uuid, new ArrayList<>()).stream()
                .map(this::getDeathChest)
                .filter(Objects::nonNull)
                .toList();
    }

    public void removeDeathChest(DeathChest chest, boolean drop){
        ownerChests.getOrDefault(chest.getOwnerUUID(), new ArrayList<>()).remove(chest.getChestUUID());
        deathChestMap.remove(chest.getChestUUID());

        chest.stopTime(drop);
    }

    public Collection<DeathChest> getAllDeathChest(){
        return Collections.unmodifiableCollection(deathChestMap.values());
    }

    private DeathChest addDeathChestToCache(DeathChest deathChest){
        ownerChests.computeIfAbsent(deathChest.getOwnerUUID(), k -> new ArrayList<>())
                .add(deathChest.getChestUUID());

        return deathChestMap.put(deathChest.getChestUUID(), deathChest);
    }

    public DeathChest createDeathChest(Player player){

        if (player.getInventory().isEmpty()) return null;

        Map<Integer, ItemStack> mapItems = new HashMap<>();

        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            ItemStack itemStack = player.getInventory().getItem(i);

            if (itemStack != null && itemStack.getType() != Material.AIR){
                mapItems.put(i, itemStack);
            }
        }


        DeathChest deathChest = new DeathChest(player.getUniqueId(), mapItems, player.getLocation());
        deathChest.spawn();


        return addDeathChestToCache(deathChest);
    }

    public void load(){
        File deathFile = new File(JpDeathChest.getInstance().getDataFolder(), DEATHCHEST_FILE);

        if (!deathFile.exists()){
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(deathFile);

        ConfigurationSection deathSections = config.getConfigurationSection("chest");

        if (deathSections != null){
            for (String key : deathSections.getKeys(false)){
                ConfigurationSection chestSection = config.getConfigurationSection("chest." + key);

                if (chestSection == null) continue;

                String chestUUIDStr = chestSection.getString("chestuuid");
                if (chestUUIDStr == null) continue;
                UUID chestUUID = UUID.fromString(chestUUIDStr);

                String ownerUUIDStr = chestSection.getString("owneruuid");
                if (ownerUUIDStr == null) continue;
                UUID ownerUUID = UUID.fromString(ownerUUIDStr);

                Location chestLocation = chestSection.getLocation("chestlocation");
                if (chestLocation == null) continue;

                int time = chestSection.getInt("time");

                Map<Integer, ItemStack> mapItems = new HashMap<>();


                ConfigurationSection itemsSection = chestSection.getConfigurationSection("items");
                if (itemsSection != null){
                    for (String itemKey : itemsSection.getKeys(false)){
                        int slot = Integer.parseInt(itemKey);
                        ItemStack itemStack = itemsSection.getItemStack(itemKey);

                        if (itemStack != null){
                            mapItems.put(slot, itemStack);
                        }
                    }
                }

                DeathChest deathChest = new DeathChest(chestUUID, time, ownerUUID, mapItems, chestLocation);
                deathChest.spawn();

                addDeathChestToCache(deathChest);
            }
        }

        if (!deathFile.delete()){
            try (FileWriter writer = new FileWriter(deathFile)){
                writer.write("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void stop(){
        File deathFile = new File(JpDeathChest.getInstance().getDataFolder(), DEATHCHEST_FILE);

        if (!deathFile.exists()){
            if (deathFile.getParentFile() != null) deathFile.getParentFile().mkdirs();

            try {
                deathFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(deathFile);

        for (DeathChest chest : deathChestMap.values()){
            UUID chestUUID = chest.getChestUUID();
            UUID ownerUUID = chest.getOwnerUUID();
            Location chestLocation = chest.getChestLocation();
            Map<Integer, ItemStack> mapItems = chest.getMapItems();

            config.set("chest." + chestUUID + ".chestuuid", chestUUID.toString());
            config.set("chest." + chestUUID + ".owneruuid", ownerUUID.toString());
            config.set("chest." + chestUUID + ".chestlocation", chestLocation);
            config.set("chest." + chestUUID + ".time", chest.getTime());


            for (Map.Entry<Integer, ItemStack> entry : mapItems.entrySet()){
                config.set("chest." + chestUUID + ".items." + entry.getKey(), entry.getValue());
            }

            chest.stopTime(false);
        }

        try {
            config.save(deathFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        deathChestMap.clear();
    }

}
