package dev.gdataa.hyvel.listeners;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.menus.MenuManager;
import dev.gdataa.hyvel.pets.PetModel;
import dev.gdataa.hyvel.pets.PetRegistry;
import dev.gdataa.hyvel.pets.PetVisualManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PetsGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (!title.equals("§8Menu de Pets") && !title.equals("§8Meus Pets") && !title.equals("§8Coleção de Pets")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (title.equals("§8Menu de Pets")) {
            switch (slot) {
                case 10: MenuManager.openMyPetsMenu(player); break;
                case 12: MenuManager.openCollectionMenu(player); break;
                case 14: player.sendMessage("§b[!] Sistema de Evolução em breve!"); break;
                case 16: player.sendMessage("§6[!] Loja de Pets em breve!"); break;
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

                        // Desativa se já for o visual ativo
                        if (currentVisual != null && currentVisual.equals(equippedId)) {
                            PetsMain.db().setVisualPet(player.getUniqueId(), null);
                            player.sendMessage("§cSeu pet não está mais visível.");
                            // TODO: Remover ArmorStand
                        } else {
                            // Define como novo visual
                            PetsMain.db().setVisualPet(player.getUniqueId(), equippedId);
                            player.sendMessage("§dSeu pet " + PetRegistry.getById(equippedId).getNome() + " §dagora está flutuando!");
                            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1, 1);
                            // TODO: Spawnar ArmorStand
                        }
                    }
                    // SE CLICOU COM O BOTÃO DIREITO (Definir Visual)
                    if (event.isRightClick()) {
                        String currentVisual = PetsMain.db().getVisualPet(player.getUniqueId());

                        // Desativa se já for o visual ativo
                        if (currentVisual != null && currentVisual.equals(equippedId)) {
                            PetsMain.db().setVisualPet(player.getUniqueId(), null);
                            player.sendMessage("§cSeu pet não está mais visível.");
                            PetVisualManager.despawnPet(player.getUniqueId()); // <--- AQUI (Despawn)
                        } else {
                            // Define como novo visual
                            PetsMain.db().setVisualPet(player.getUniqueId(), equippedId);
                            player.sendMessage("§dSeu pet " + PetRegistry.getById(equippedId).getNome() + " §dagora está flutuando!");
                            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1, 1);
                            PetVisualManager.spawnPet(player, equippedId); // <--- AQUI (Spawn)
                        }
                    }
                    // SE CLICOU COM O BOTÃO ESQUERDO (Desequipar)
                    else if (event.isLeftClick()) {
                        PetsMain.db().equipPet(player.getUniqueId(), null, greenIndex);

                        // Se era o visual, remove o visual também para não ficar flutuando um pet desequipado
                        String currentVisual = PetsMain.db().getVisualPet(player.getUniqueId());
                        if (currentVisual != null && currentVisual.equals(equippedId)) {
                            PetsMain.db().setVisualPet(player.getUniqueId(), null);
                            PetVisualManager.despawnPet(player.getUniqueId()); // <--- AQUI (Despawn)
                        }

                        player.sendMessage("§cPet desequipado!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 0.5f);
                    }

                    MenuManager.openMyPetsMenu(player); // Atualiza o menu
                }
                return;
            }

            // 2. EQUIPAR
            if (isCollectionSlot(slot)) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                PetModel clickedPet = PetRegistry.getByName(displayName);
                if (clickedPet == null) return;

                UUID uuid = player.getUniqueId();
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
                    MenuManager.openMyPetsMenu(player);
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