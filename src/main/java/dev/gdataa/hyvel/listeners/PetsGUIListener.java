package dev.gdataa.hyvel.listeners;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.menus.MenuManager;
import dev.gdataa.hyvel.pets.PetModel;
import dev.gdataa.hyvel.pets.PetRegistry;
import dev.gdataa.hyvel.pets.PetVisualManager;
import dev.gdataa.hyvel.pets.items.ClothingRegistry;
import dev.gdataa.hyvel.pets.items.PetClothing;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetsGUIListener implements Listener {

    // Mapa para evitar duplo clique (Cooldown de 500ms)
    private final Map<UUID, Long> clickCooldown = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (!title.equals("§8Menu de Pets") &&
                !title.equals("§8Meus Pets") &&
                !title.equals("§8Coleção de Pets") &&
                !title.equals("§8Loja de Pets") &&
                !title.equals("§8Escolha um Pet para Evoluir") &&
                !title.startsWith("§8Gerir: ") &&
                !title.startsWith("§8Guarda-Roupa: ") &&
                !title.equals("§8Loja: Roupas") &&
                !title.equals("§8Loja: Comidas")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        // --- SISTEMA ANTI DOUBLE-CLICK ---
        UUID uuid = player.getUniqueId();
        if (clickCooldown.containsKey(uuid) && System.currentTimeMillis() - clickCooldown.get(uuid) < 500) {
            return; // Se passou menos de 500ms, ignora o clique fantasma
        }
        clickCooldown.put(uuid, System.currentTimeMillis());
        // ---------------------------------

        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (title.equals("§8Menu de Pets")) {
            switch (slot) {
                case 10: MenuManager.openMyPetsMenu(player); break;
                case 12: MenuManager.openCollectionMenu(player); break;
                case 14: MenuManager.openEvolutionSelector(player); break;
                case 16: MenuManager.openPetShopMenu(player); break;
            }
            return;
        }

        if (title.equals("§8Meus Pets")) {
            if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                MenuManager.openMainMenu(player);
                return;
            }

            // 1. SLOTS VERDES (Desequipar ou Definir Visual)
            int greenIndex = getGreenSlotIndex(slot);
            if (greenIndex != -1) {
                String equippedId = PetsMain.db().getEquippedPet(player.getUniqueId(), greenIndex);

                if (equippedId != null) {
                    // SE CLICOU COM O BOTÃO DIREITO (Definir Visual)
                    if (event.isRightClick()) {
                        String currentVisual = PetsMain.db().getVisualPet(player.getUniqueId());

                        if (currentVisual != null && currentVisual.equals(equippedId)) {
                            PetsMain.db().setVisualPet(player.getUniqueId(), null);
                            player.sendMessage("§cSeu pet não está mais visível.");
                            PetVisualManager.despawnPet(player.getUniqueId());
                        } else {
                            PetsMain.db().setVisualPet(player.getUniqueId(), equippedId);
                            player.sendMessage("§dSeu pet " + PetRegistry.getById(equippedId).getNome() + " §dagora está flutuando!");
                            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1, 1);
                            PetVisualManager.spawnPet(player, equippedId);
                        }
                    }
                    // SE CLICOU COM O BOTÃO ESQUERDO (Desequipar)
                    else if (event.isLeftClick()) {
                        PetsMain.db().equipPet(player.getUniqueId(), null, greenIndex);

                        String currentVisual = PetsMain.db().getVisualPet(player.getUniqueId());
                        if (currentVisual != null && currentVisual.equals(equippedId)) {
                            PetsMain.db().setVisualPet(player.getUniqueId(), null);
                            PetVisualManager.despawnPet(player.getUniqueId());
                        }

                        player.sendMessage("§cPet desequipado!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0.5f);
                    }

                    Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                        MenuManager.openMyPetsMenu(player);
                    });
                }
                return;
            }

            // 2. EQUIPAR
            if (isCollectionSlot(slot)) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                PetModel clickedPet = PetRegistry.getByName(displayName);
                if (clickedPet == null) return;

                int maxSlotsPermitidos = Math.min(MenuManager.getMaxPets(player), 4);

                for (int i = 0; i < 4; i++) {
                    String eqId = PetsMain.db().getEquippedPet(uuid, i);
                    if (eqId != null && eqId.equalsIgnoreCase(clickedPet.getId())) {
                        player.sendMessage("§cEste pet já está equipado!");
                        return;
                    }
                }

                int targetSlot = -1;
                for (int i = 0; i < maxSlotsPermitidos; i++) {
                    if (PetsMain.db().getEquippedPet(uuid, i) == null) {
                        targetSlot = i;
                        break;
                    }
                }

                if (targetSlot != -1) {
                    PetsMain.db().equipPet(uuid, clickedPet.getId(), targetSlot);
                    player.sendMessage("§aPet " + clickedPet.getNome() + " §aequipado!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

                    Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                        MenuManager.openMyPetsMenu(player);
                    });
                } else {
                    player.sendMessage("§cLimite de slots atingido (" + maxSlotsPermitidos + ")!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                }
            }
        }

        if (title.equals("§8Coleção de Pets")) {
            if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                MenuManager.openMainMenu(player);
            }
            return;
        }

        if (title.equals("§8Loja de Pets")) {
            if (slot == 22) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openMainMenu(player);
                });
                return;
            }

            if (slot == 11) {
                player.sendMessage("§bAbrindo loja de roupas...");
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                // TODO: Abrir Menu da Loja (Categoria Roupas)
            } else if (slot == 13) {
                player.sendMessage("§eAbrindo loja de comidas...");
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                // TODO: Abrir Menu da Loja (Categoria Comidas)
            } else if (slot == 15) {
                player.sendMessage("§6Bem-vindo ao Leilão!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                // TODO: Abrir Menu do Leilão
            }
            return;
        }

        // --- MENU SELETOR DE EVOLUÇÃO ---
        if (title.equals("§8Escolha um Pet para Evoluir")) {
            if (slot == 22) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openMainMenu(player);
                });
                return;
            }

            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                if (displayName.equals("§cVoltar")) return;

                PetModel clickedPet = PetRegistry.getByName(displayName);
                if (clickedPet != null) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                        MenuManager.openPetProfile(player, clickedPet.getId());
                    });
                }
            }
            return;
        }

        // --- MENU DE PERFIL DO PET (Gerir) ---
        if (title.startsWith("§8Gerir: ")) {
            String petName = title.replace("§8Gerir: ", "").trim();
            PetModel currentPet = PetRegistry.getByName(petName);
            if (currentPet == null) return;

            if (slot == 22) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openEvolutionSelector(player);
                });
                return;
            }

            if (slot == 11) {
                player.sendMessage("§eAbrindo inventário de comidas...");
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                // TODO: Abrir GUI com as comidas que o jogador tem
            } else if (slot == 15) {
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openWardrobeMenu(player, currentPet.getId());
                });
            }
            return;
        }

        // --- MENU: GUARDA-ROUPA ---
        if (title.startsWith("§8Guarda-Roupa: ")) {
            String petName = title.replace("§8Guarda-Roupa: ", "").trim();
            PetModel currentPet = PetRegistry.getByName(petName);
            if (currentPet == null) return;

            if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openPetProfile(player, currentPet.getId());
                });
                return;
            }

            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                if (displayName.equals("§cVoltar")) return;

                // Tenta encontrar a roupa baseada no nome
                String cleanName = displayName.replaceAll("(?i)§[0-9A-FK-ORX]", "").trim();
                PetClothing clickedClothing = null;
                for (PetClothing c : ClothingRegistry.getAllClothing()) {
                    if (c.getNome().replaceAll("(?i)§[0-9A-FK-ORX]", "").trim().equalsIgnoreCase(cleanName)) {
                        clickedClothing = c;
                        break;
                    }
                }

                if (clickedClothing != null) {
                    String equippedClothingId = PetsMain.db().getEquippedClothing(player.getUniqueId(), currentPet.getId());

                    // Se clicou na roupa que já está equipada -> Desequipa
                    if (equippedClothingId != null && equippedClothingId.equals(clickedClothing.getId())) {
                        PetsMain.db().equipClothing(player.getUniqueId(), currentPet.getId(), null);
                        player.sendMessage("§cRoupa desequipada do " + currentPet.getNome() + "!");
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 0.5f);
                    }
                    // Senão -> Equipa a nova roupa
                    else {
                        PetsMain.db().equipClothing(player.getUniqueId(), currentPet.getId(), clickedClothing.getId());
                        player.sendMessage("§aVocê equipou " + clickedClothing.getNome() + " §ano pet " + currentPet.getNome() + "!");
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.2f);
                    }

                    // Atualiza o menu de roupas para refletir a mudança
                    Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                        MenuManager.openWardrobeMenu(player, currentPet.getId());
                    });
                }
            }
            return;
        }
    }

    private int getGreenSlotIndex(int slot) {
        return switch (slot) {
            case 10 -> 0;
            case 19 -> 1;
            case 28 -> 2;
            case 37 -> 3;
            default -> -1;
        };
    }

    private boolean isCollectionSlot(int s) {
        return (s >= 12 && s <= 16) || (s >= 21 && s <= 25) || (s >= 30 && s <= 34) || (s >= 39 && s <= 43);
    }

    @EventHandler
    public void onPetInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (displayName.startsWith("§6Pet: ")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            PetModel pet = PetRegistry.getByName(displayName);

            if (pet == null) return;

            if (PetsMain.db().getUnlockedPets(player.getUniqueId()).contains(pet.getId())) {
                player.sendMessage("§cVocê já possui este pet!");
                return;
            }

            PetsMain.db().unlockPet(player.getUniqueId(), pet.getId());
            item.setAmount(item.getAmount() - 1);

            player.sendMessage("§a§lOBBA! §7O pet " + pet.getNome() + " §7foi adicionado à sua coleção!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        }
    }
}