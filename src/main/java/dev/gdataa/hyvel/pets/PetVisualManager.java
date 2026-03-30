package dev.gdataa.hyvel.pets;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetVisualManager {

    // Criamos uma mini-classe para agrupar as duas partes do pet
    private static class ActivePet {
        ArmorStand headStand;
        ArmorStand nameStand;

        public ActivePet(ArmorStand headStand, ArmorStand nameStand) {
            this.headStand = headStand;
            this.nameStand = nameStand;
        }

        public void remove() {
            if (headStand != null && !headStand.isDead()) headStand.remove();
            if (nameStand != null && !nameStand.isDead()) nameStand.remove();
        }

        public boolean isDead() {
            return headStand.isDead() || nameStand.isDead();
        }
    }

    // Agora guardamos o ActivePet (que contém os dois ArmorStands)
    private static final Map<UUID, ActivePet> activePets = new HashMap<>();

    public static void spawnPet(Player player, String petId) {
        despawnPet(player.getUniqueId());

        PetModel pet = PetRegistry.getById(petId);
        if (pet == null) return;

        Location loc = player.getLocation();

        // 1. ArmorStand da Cabeça (Textura)
        ArmorStand headStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        headStand.setVisible(false);
        headStand.setInvulnerable(true);
        headStand.setGravity(false);
        headStand.setSmall(true);
        headStand.setBasePlate(false);
        headStand.setArms(false);
        headStand.setMarker(true); // Impede que bloqueie cliques do jogador

        ItemStack petHead = MenuManager.createCustomHead(pet.getTextura(), "pet");
        headStand.getEquipment().setHelmet(petHead);

        // 2. ArmorStand do Nome (Holograma)
        ArmorStand nameStand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0, 0.7, 0), EntityType.ARMOR_STAND);
        nameStand.setVisible(false);
        nameStand.setInvulnerable(true);
        nameStand.setGravity(false);
        nameStand.setSmall(true);
        nameStand.setMarker(true);
        nameStand.setCustomName("§a" + pet.getNome());
        nameStand.setCustomNameVisible(true);

        activePets.put(player.getUniqueId(), new ActivePet(headStand, nameStand));
    }

    public static void despawnPet(UUID uuid) {
        if (activePets.containsKey(uuid)) {
            activePets.get(uuid).remove();
            activePets.remove(uuid);
        }
    }

    public static void despawnAll() {
        for (ActivePet pet : activePets.values()) {
            pet.remove();
        }
        activePets.clear();
    }

    public static void startFollowTask(PetsMain plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, ActivePet> entry : activePets.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    ActivePet pet = entry.getValue();

                    if (player == null || !player.isOnline() || pet.isDead()) {
                        pet.remove();
                        continue;
                    }

                    if (!pet.headStand.getWorld().equals(player.getWorld())) {
                        pet.headStand.teleport(player.getLocation());
                        pet.nameStand.teleport(player.getLocation());
                    }

                    Location pLoc = player.getLocation();
                    double yaw = Math.toRadians(pLoc.getYaw());

                    double offsetX = -Math.cos(yaw) * 1.0;
                    double offsetZ = -Math.sin(yaw) * 1.0;
                    double floatOffset = Math.sin(System.currentTimeMillis() / 400.0) * 0.15;

                    Location targetLoc = pLoc.clone().add(offsetX, 1.2 + floatOffset, offsetZ);
                    targetLoc.setYaw(pLoc.getYaw());

                    Location currentLoc = pet.headStand.getLocation();

                    if (currentLoc.distanceSquared(targetLoc) > 100) {
                        pet.headStand.teleport(targetLoc);
                        pet.nameStand.teleport(targetLoc.clone().add(0, 0.7, 0)); // Nome sempre 0.7 blocos acima
                    } else {
                        Vector direction = targetLoc.toVector().subtract(currentLoc.toVector());
                        currentLoc.add(direction.multiply(0.3));
                        currentLoc.setYaw(pLoc.getYaw());

                        pet.headStand.teleport(currentLoc);

                        // Teleporta o nome um pouco acima da cabeça para o efeito perfeito
                        Location nameLoc = currentLoc.clone().add(0, 0.9, 0);
                        pet.nameStand.teleport(nameLoc);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}