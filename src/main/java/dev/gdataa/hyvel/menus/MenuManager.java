package dev.gdataa.hyvel.menus;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.pets.PetModel;
import dev.gdataa.hyvel.pets.PetRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.*;

public class MenuManager {

    public static void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "§8Menu de Pets");
        gui.setItem(10, createItem(Material.NAME_TAG, "§aMeus Pets", "§7Gerencie seus pets ativos."));
        gui.setItem(12, createItem(Material.BOOK, "§eColeção", "§7Veja os pets encontrados e os que faltam."));
        gui.setItem(14, createItem(Material.EXPERIENCE_BOTTLE, "§bEvolução", "§7Melhore as habilidades do seu pet."));
        gui.setItem(16, createItem(Material.EMERALD, "§6Loja de Pets", "§7Compre novos pets e cosméticos."));
        player.openInventory(gui);
    }

    public static void openMyPetsMenu(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, "§8Meus Pets");
        int maxPets = getMaxPets(player);

        int[] greenSlots = {10, 19, 28, 37};
        String visualPetId = PetsMain.db().getVisualPet(player.getUniqueId());

        for (int i = 0; i < greenSlots.length; i++) {
            if (i < maxPets) {
                String equippedPetId = PetsMain.db().getEquippedPet(player.getUniqueId(), i);

                if (equippedPetId != null) {
                    PetModel pet = PetRegistry.getById(equippedPetId);
                    if (pet != null) {
                        List<String> lore = new ArrayList<>();
                        lore.add(pet.getRaridade());
                        lore.add("");
                        lore.addAll(pet.getBonus());
                        lore.add("");

                        // Lógica da Lore Visual
                        if (visualPetId != null && visualPetId.equals(equippedPetId)) {
                            lore.add("§d§lPET VISUAL ATIVO §d");
                            lore.add("§7(Aparecendo ao seu lado)");
                            lore.add("");
                        } else {
                            lore.add("§e§lCLIQUE DIREITO");
                            lore.add("§7Para fazer este pet flutuar.");
                            lore.add("");
                        }

                        lore.add("§c§lCLIQUE ESQUERDO PARA DESEQUIPAR");

                        gui.setItem(greenSlots[i], createCustomHead(pet.getTextura(), "§a" + pet.getNome(), lore.toArray(new String[0])));
                    }
                } else {
                    gui.setItem(greenSlots[i], createItem(Material.LIME_STAINED_GLASS_PANE, "§aSlot Vazio " + (i + 1), "§7Clique em um pet da sua", "§7coleção para equipá-lo aqui."));
                }
            } else {
                gui.setItem(greenSlots[i], createItem(Material.RED_STAINED_GLASS_PANE, "§cSlot Bloqueado", "§7Evolua seu rank para liberar!"));
            }
        }

        int[] collectionSlots = {
                12, 13, 14, 15, 16,
                21, 22, 23, 24, 25,
                30, 31, 32, 33, 34,
                39, 40, 41, 42, 43
        };

        List<String> unlockedIds = PetsMain.db().getUnlockedPets(player.getUniqueId());
        int slotIndex = 0;

        for (String petId : unlockedIds) {
            if (slotIndex >= collectionSlots.length) break;
            PetModel pet = PetRegistry.getById(petId);
            if (pet != null) {
                List<String> lore = new ArrayList<>();
                lore.add(pet.getRaridade());
                lore.add("");
                lore.addAll(pet.getBonus());
                lore.add("");
                lore.add("§a§lCLIQUE PARA EQUIPAR");
                gui.setItem(collectionSlots[slotIndex], createCustomHead(pet.getTextura(), "§e" + pet.getNome(), lore.toArray(new String[0])));
                slotIndex++;
            }
        }

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(49, createCustomHead(voltarValue, "§cVoltar", "§7Voltar ao Menu Principal."));
        player.openInventory(gui);
    }

    public static void openCollectionMenu(Player player) {
        Inventory gui = Bukkit.createInventory(player, 54, "§8Coleção de Pets");
        List<String> unlockedIds = PetsMain.db().getUnlockedPets(player.getUniqueId());
        List<PetModel> allPets = PetRegistry.getAllPets();

        String lockedBase64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19";
        int[] slotsPermitidos = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (PetModel pet : allPets) {
            if (index >= slotsPermitidos.length) break;
            int slotAtual = slotsPermitidos[index];

            if (unlockedIds.contains(pet.getId())) {
                List<String> lore = new ArrayList<>();
                lore.add(pet.getRaridade());
                lore.add("");
                lore.addAll(pet.getBonus());
                lore.add("");
                lore.add("§aStatus: Desbloqueado");
                gui.setItem(slotAtual, createCustomHead(pet.getTextura(), "§a" + pet.getNome(), lore.toArray(new String[0])));
            } else {
                List<String> lore = new ArrayList<>();
                lore.add(pet.getRaridade());
                lore.add("");
                lore.add("§7§oBônus desconhecido...");
                lore.add("");
                lore.add("§cStatus: Bloqueado");
                lore.add("§7Encontre este pet para revelar!");
                gui.setItem(slotAtual, createCustomHead(lockedBase64, "§cPet Desconhecido", lore.toArray(new String[0])));
            }
            index++;
        }

        String progressoValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDhiODFkODkzNWZjMTAxZTM1ZjNlNTRmNTM3YjYxMjgyMmYxOGZhZjc1MmE5MDAzN2NjNDIxNWE1ZGM4ODFlZiJ9fX0=";
        gui.setItem(48, createCustomHead(progressoValue, "§bSeu Progresso", "§7Pets Encontrados: §f" + unlockedIds.size() + "/" + allPets.size()));

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(49, createCustomHead(voltarValue, "§cVoltar", "§7Voltar ao Menu Principal."));
        player.openInventory(gui);
    }

    public static int getMaxPets(Player player) {
        if (player.hasPermission("hyvel.pets.5")) return 5;
        if (player.hasPermission("hyvel.pets.4")) return 4;
        if (player.hasPermission("hyvel.pets.3")) return 3;
        if (player.hasPermission("hyvel.pets.2")) return 2;
        return 1;
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createCustomHead(String base64Value, String name, String... lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));

            UUID uuid = UUID.nameUUIDFromBytes(base64Value.getBytes());
            PlayerProfile profile = Bukkit.createPlayerProfile(uuid, null);
            PlayerTextures textures = profile.getTextures();

            try {
                String decoded = new String(Base64.getDecoder().decode(base64Value));
                String url = decoded.substring(decoded.indexOf("url\":\"") + 6, decoded.lastIndexOf("\""));
                textures.setSkin(new URL(url));
                profile.setTextures(textures);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Erro ao carregar textura Base64 para o pet: " + name);
            }

            meta.setOwnerProfile(profile);
            head.setItemMeta(meta);
        }
        return head;
    }
}