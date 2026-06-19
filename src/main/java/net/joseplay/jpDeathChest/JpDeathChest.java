package net.joseplay.jpDeathChest;

import net.joseplay.allianceutils.Allianceutils;
import net.joseplay.allianceutils.api.extensions.Alliance;
import net.joseplay.allianceutils.api.extensions.AlliancePlugin;
import net.joseplay.jpDeathChest.data.DeathChestManager;
import net.joseplay.jpDeathChest.listeners.DeathChestEvents;

public final class JpDeathChest extends AlliancePlugin {
    private static JpDeathChest instance;
    private DeathChestManager deathChestManager;

    @Override
    public void onEnable(Allianceutils plugin) {
        instance = this;
        deathChestManager = new DeathChestManager();
        deathChestManager.load();

        Alliance.getAllianceListenerManager().registerListener(this, new DeathChestEvents());
    }

    @Override
    public void onDisable() {
        deathChestManager.stop();
    }


    public static JpDeathChest getInstance() {
        return instance;
    }

    public DeathChestManager getDeathChestManager() {
        return deathChestManager;
    }
}
