package net.joseplay.jpDeathChest.gui;

import net.joseplay.allianceutils.api.menu.CreateItem;
import net.joseplay.allianceutils.api.menu.SimpleMenu;
import net.joseplay.jpDeathChest.JpDeathChest;
import net.joseplay.jpDeathChest.entities.DeathChest;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;

public class DeathChestGUI extends SimpleMenu {
    private final DeathChest deathChest;

    public DeathChestGUI(DeathChest deathChest) {
        super(Rows.SIX_LINE, "§cBaú da morte");
        this.deathChest = deathChest;
    }

    @Override
    public void onSetItems() {
        for (ItemStack itemStack : deathChest.getMapItems().values()){
            addItem(itemStack);
        }

        ItemStack colletButton = CreateItem.createItemStack(
                "§aColetar",
                List.of("§7Clique para coletar."),
                Material.CHEST
        );

        ItemStack deleteButton = CreateItem.createItemStack(
                "§cDeletar",
                List.of("§7Clique para deletar."),
                Material.LAVA_BUCKET
        );


        setItem(48, deleteButton, event -> {
            event.getWhoClicked().closeInventory();
            JpDeathChest.getInstance().getDeathChestManager().removeDeathChest(deathChest, false);
        });

        setItem(50, colletButton, event -> {
            event.getWhoClicked().closeInventory();
            setItemsPlayer(deathChest.getMapItems(), (Player) event.getWhoClicked());

            JpDeathChest.getInstance().getDeathChestManager().removeDeathChest(deathChest, false);
        });
    }

    private void setItemsPlayer(Map<Integer, ItemStack> mapItems, Player player){

        PlayerInventory playerInventory = player.getInventory();
        for (Map.Entry<Integer, ItemStack> entry : mapItems.entrySet()){
            int slot = entry.getKey();
            ItemStack itemStack = entry.getValue();

            if (playerInventory.getItem(slot) != null){
                player.getWorld().dropItem(player.getLocation(), itemStack);
                continue;
            }

            playerInventory.setItem(slot, itemStack);
        }

    }
}
