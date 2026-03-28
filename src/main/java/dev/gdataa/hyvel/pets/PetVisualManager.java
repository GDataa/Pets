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

    // Guarda os ArmorStands ativos de cada jogador
    private static final Map<UUID, ArmorStand> activePets = new HashMap<>();

    public static void spawnPet(Player player, String petId) {
        despawnPet(player.getUniqueId()); // Remove se já existir um

        PetModel pet = PetRegistry.getById(petId);
        if (pet == null) return;

        Location loc = player.getLocation();
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

        // Configurações do ArmorStand para ser um "Pet"
        stand.setVisible(false);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setMarker(true); // Otimiza a hitbox (não atrapalha os cliques do jogador)

        // Nome flutuante
        stand.setCustomName("§a" + pet.getNome());
        stand.setCustomNameVisible(true);

        // Equipar a cabeça do pet
        ItemStack petHead = MenuManager.createCustomHead(pet.getTextura(), "pet");
        stand.getEquipment().setHelmet(petHead);

        activePets.put(player.getUniqueId(), stand);
    }

    public static void despawnPet(UUID uuid) {
        if (activePets.containsKey(uuid)) {
            ArmorStand stand = activePets.get(uuid);
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
            activePets.remove(uuid);
        }
    }

    public static void despawnAll() {
        for (ArmorStand stand : activePets.values()) {
            if (stand != null && !stand.isDead()) stand.remove();
        }
        activePets.clear();
    }

    // O "Motor" que faz os pets seguirem os jogadores com efeito de flutuação
    public static void startFollowTask(PetsMain plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, ArmorStand> entry : activePets.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    ArmorStand pet = entry.getValue();

                    // Se o jogador saiu ou o pet sumiu, removemos da lista
                    if (player == null || !player.isOnline() || pet.isDead()) {
                        pet.remove();
                        continue;
                    }

                    // Se o jogador mudou de mundo, o pet precisa de ser teleportado instantaneamente para o mesmo mundo
                    if (!pet.getWorld().equals(player.getWorld())) {
                        pet.teleport(player.getLocation());
                    }

                    Location pLoc = player.getLocation();
                    double yaw = Math.toRadians(pLoc.getYaw());

                    // Posição base: Atrás e um pouco para o lado
                    double offsetX = -Math.cos(yaw) * 1.0;
                    double offsetZ = -Math.sin(yaw) * 1.0;

                    // --- EFEITO DE FLUTUAÇÃO (Seno) ---
                    // System.currentTimeMillis() / 400.0 controla a velocidade do sobe/desce
                    // * 0.15 controla a altura máxima da flutuação
                    double floatOffset = Math.sin(System.currentTimeMillis() / 400.0) * 0.15;

                    // Aplicamos a altura base (1.2) + o efeito de flutuação
                    Location targetLoc = pLoc.clone().add(offsetX, 1.2 + floatOffset, offsetZ);
                    targetLoc.setYaw(pLoc.getYaw()); // Pet olha para onde o player olha

                    Location currentLoc = pet.getLocation();

                    // Se o jogador estiver muito longe (ex: usou /tp ou voou muito rápido), teleporta direto
                    if (currentLoc.distanceSquared(targetLoc) > 100) { // Usar distanceSquared é melhor para performance
                        pet.teleport(targetLoc);
                    } else {
                        // Movimento suave (Interpolação)
                        Vector direction = targetLoc.toVector().subtract(currentLoc.toVector());
                        // O 0.3 dita a suavidade de quão rápido ele tenta alcançar o targetLoc
                        currentLoc.add(direction.multiply(0.3));
                        currentLoc.setYaw(pLoc.getYaw());

                        pet.teleport(currentLoc);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L); // Roda a cada tick (20 vezes por segundo)
    }
}
