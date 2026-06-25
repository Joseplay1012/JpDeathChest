package net.joseplay.jpDeathChest.utils;

import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleUtils {


    public static void spawnBeam(Location location, Particle particle, int heigth){

        for (double i = 0; i < heigth; i += 0.5) {
            location.getWorld().spawnParticle(particle, location.clone().add(0, i, 0), 1, 0, 0,0, 0);
        }
    }

    public static void spawnSpiral(Location location, Particle particle, double heigth, double radius, int points, int offSet){
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI / i * points + offSet;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;


            Location loc = location.clone().add(x, heigth , z);

            loc.getWorld().spawnParticle(particle, loc, 1, 0,0,0,0);
        }

    }

}
