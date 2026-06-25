package net.joseplay.jpDeathChest.gui;

import net.joseplay.allianceutils.Utils.UnicodeFontReplace;
import net.joseplay.allianceutils.api.menu.CreateItem;
import net.joseplay.allianceutils.api.menu.PagedCustomMenu;
import net.joseplay.jpDeathChest.JpDeathChest;
import net.joseplay.jpDeathChest.entities.DeathChest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DeathChestsGUI extends PagedCustomMenu {
    public DeathChestsGUI(RowsStyle style) {
        super(style.getRows(), "§©Baus ativos", style.getSlots(), style.getNextPage(), style.getPreviusPage());
    }

    @Override
    public void onSetItems() {
        List<DeathChest> deathChests = new ArrayList<>(JpDeathChest.getInstance().getDeathChestManager().getAllDeathChest());


        if (deathChests.isEmpty()){
            setFixedItem(22, CreateItem.createItemStack(
                    "§cSem dados!",
                    List.of(),
                    Material.BARRIER
            ));

            update();
            return;
        }


        for (DeathChest deathChest : deathChests){
            OfflinePlayer player1 = Bukkit.getOfflinePlayer(deathChest.getOwnerUUID());
            ItemStack item = CreateItem.createItemStack(
                    "§a" + player1.getName(),
                    Stream.of(
                            "",
                            "§aTempo: §e" + deathChest.getTimeParsed(),
                            "§aLocalização: §e" + deathChest.getLocationParsed(),
                            "§aTotal de Itens: §e" + deathChest.getMapItems().size(),
                            "",
                            "§cClique com o direito para teleportar ao ele.",
                            "§aClique com o esquerdo para abrir o baú."
                    ).map(UnicodeFontReplace::allianceFontReplace).toList(),
                    Material.CHEST
            );

            addItem(item, event -> {
                Player who = (Player) event.getWhoClicked();

                if (event.isRightClick()){
                    who.closeInventory();
                    who.teleport(deathChest.getChestLocation());
                } else if (event.isLeftClick()){

                    if (!who.hasPermission("alc.admin")){
                        who.closeInventory();
                        return;
                    }

                    new DeathChestGUI(deathChest).open(who);
                }


            });
        }

        update();
    }
}
