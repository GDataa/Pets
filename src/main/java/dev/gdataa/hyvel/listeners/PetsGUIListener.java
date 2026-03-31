package dev.gdataa.hyvel.listeners;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.menus.MenuManager;
import dev.gdataa.hyvel.pets.PetModel;
import dev.gdataa.hyvel.pets.PetRegistry;
import dev.gdataa.hyvel.pets.PetVisualManager;
import dev.gdataa.hyvel.pets.items.ClothingRegistry;
import dev.gdataa.hyvel.pets.items.PetClothing;
import dev.gdataa.hyvel.shops.AuctionItem;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PetsGUIListener implements Listener {

    private final Map<UUID, Long> clickCooldown = new HashMap<>();
    public static final Map<UUID, UUID> pendingBids = new HashMap<>();

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
                !title.equals("§8Loja: Comidas") &&
                !title.startsWith("§8Leilão Ativo")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        UUID uuid = player.getUniqueId();
        if (clickCooldown.containsKey(uuid) && System.currentTimeMillis() - clickCooldown.get(uuid) < 500) {
            return;
        }
        clickCooldown.put(uuid, System.currentTimeMillis());

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

            int greenIndex = getGreenSlotIndex(slot);
            if (greenIndex != -1) {
                String equippedId = PetsMain.db().getEquippedPet(player.getUniqueId(), greenIndex);

                if (equippedId != null) {
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
                    } else if (event.isLeftClick()) {
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
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openClothingShop(player);
                });
            } else if (slot == 13) {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openFoodShop(player);
                });
            } else if (slot == 15) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openAuctionMenu(player, 1);
                });
            }
            return;
        }

        // --- MENU: LEILÃO ATIVO (PAGINADO) ---
        if (title.startsWith("§8Leilão Ativo")) {
            if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openPetShopMenu(player);
                });
                return;
            }
            if (slot == 48) return; // Botão de info

            // 1. Descobrir em que página estamos atualmente
            int currentPage = 1;
            try {
                if (title.contains("Pág. ")) {
                    currentPage = Integer.parseInt(title.split("Pág\\. ")[1].trim());
                }
            } catch (Exception ignored) {}

            // 2. Lógica das Setas (Página Anterior e Próxima)
            if (slot == 27 && clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                if (clickedItem.getItemMeta().hasDisplayName() && clickedItem.getItemMeta().getDisplayName().equals("§ePágina Anterior")) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    int prevPage = currentPage - 1;
                    Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                        MenuManager.openAuctionMenu(player, prevPage);
                    });
                    return;
                }
            }
            if (slot == 35 && clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                if (clickedItem.getItemMeta().hasDisplayName() && clickedItem.getItemMeta().getDisplayName().equals("§ePróxima Página")) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    int nextPage = currentPage + 1;
                    Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                        MenuManager.openAuctionMenu(player, nextPage);
                    });
                    return;
                }
            }

            // 3. Descobrir em qual dos slots permitidos o jogador clicou
            int itemIndexInArray = -1;
            for (int i = 0; i < MenuManager.AUCTION_SLOTS.length; i++) {
                if (MenuManager.AUCTION_SLOTS[i] == slot) {
                    itemIndexInArray = i;
                    break;
                }
            }

            // Se clicou num slot válido e tem um item lá
            if (itemIndexInArray != -1 && clickedItem != null && clickedItem.getType() != Material.AIR) {
                List<dev.gdataa.hyvel.shops.AuctionItem> auctions = dev.gdataa.hyvel.shops.AuctionManager.activeAuctions;

                // Calcula o ID real do leilão baseado na página atual
                int auctionIndex = ((currentPage - 1) * MenuManager.AUCTION_SLOTS.length) + itemIndexInArray;

                if (auctionIndex >= 0 && auctionIndex < auctions.size()) {
                    dev.gdataa.hyvel.shops.AuctionItem clickedAuction = auctions.get(auctionIndex);

                    if (clickedAuction.getSellerUuid().equals(player.getUniqueId())) {
                        player.sendMessage("§cVocê não pode dar um lance no seu próprio item!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        return;
                    }

                    // Prepara o chat para receber o lance
                    pendingBids.put(player.getUniqueId(), clickedAuction.getAuctionId());
                    player.closeInventory();

                    player.sendMessage(" ");
                    player.sendMessage("§a§lLEILÃO! §7Você selecionou um item de §f" + clickedAuction.getSellerName() + "§7.");
                    player.sendMessage("§7Digite no chat o valor do seu lance (ou digite 'cancelar' para sair).");
                    player.sendMessage(" ");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
            return;
        }

        // --- MENUS DAS LOJAS ROTATIVAS ---
        if (title.equals("§8Loja: Roupas") || title.equals("§8Loja: Comidas")) {
            if (slot == 39) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openPetShopMenu(player);
                });
                return;
            }
            if (slot == 40) return; // Relógio da rotação

            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                String cleanName = clickedItem.getItemMeta().getDisplayName().replaceAll("(?i)§[0-9A-FK-ORX]", "").trim();

                if (title.equals("§8Loja: Roupas")) {
                    for (PetClothing roupa : dev.gdataa.hyvel.shops.ShopManager.roupasAtuais) {
                        if (roupa.getNome().replaceAll("(?i)§[0-9A-FK-ORX]", "").trim().equalsIgnoreCase(cleanName)) {

                            if (PetsMain.db().getUnlockedClothing(player.getUniqueId()).contains(roupa.getId())) {
                                player.sendMessage("§cVocê já possui esta roupa!");
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                return;
                            }

                            // Faz a cobrança assíncrona usando o nosso novo Manager
                            dev.gdataa.hyvel.shops.EconomyManager.processPurchase(player, "coins", roupa.getPreco(),
                                    () -> { // SUCESSO
                                        PetsMain.db().unlockClothing(player.getUniqueId(), roupa.getId());
                                        player.sendMessage("§aComprou " + roupa.getNome() + " §acom sucesso!");
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                                        player.closeInventory();
                                    },
                                    () -> { // FALHA (Sem saldo)
                                        player.sendMessage("§cVocê não tem Coins suficientes (" + roupa.getPreco() + ")!");
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                    }
                            );
                            break;
                        }
                    }
                } else {
                    for (dev.gdataa.hyvel.pets.items.PetFood comida : dev.gdataa.hyvel.shops.ShopManager.comidasAtuais) {
                        if (comida.getNome().replaceAll("(?i)§[0-9A-FK-ORX]", "").trim().equalsIgnoreCase(cleanName)) {

                            dev.gdataa.hyvel.shops.EconomyManager.processPurchase(player, "coins", comida.getPreco(),
                                    () -> { // SUCESSO
                                        PetsMain.db().unlockFood(player.getUniqueId(), comida.getId());
                                        player.sendMessage("§aComprou " + comida.getNome() + " §acom sucesso!");
                                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                                        player.closeInventory();
                                    },
                                    () -> { // FALHA
                                        player.sendMessage("§cVocê não tem Coins suficientes (" + comida.getPreco() + ")!");
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                                    }
                            );
                            break;
                        }
                    }
                }
            }
            return;
        }

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

            // AQUI ESTÁ A CHAMADA QUE FALTAVA (Slot 11 = Botão de Alimentar)
            if (slot == 11) {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openFoodBackpack(player, currentPet.getId());
                });
            } else if (slot == 15) {
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openWardrobeMenu(player, currentPet.getId());
                });
            }
            return;
        }

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

                    if (equippedClothingId != null && equippedClothingId.equals(clickedClothing.getId())) {
                        PetsMain.db().equipClothing(player.getUniqueId(), currentPet.getId(), null);
                        player.sendMessage("§cRoupa desequipada do " + currentPet.getNome() + "!");
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 0.5f);
                    } else {
                        PetsMain.db().equipClothing(player.getUniqueId(), currentPet.getId(), clickedClothing.getId());
                        player.sendMessage("§aVocê equipou " + clickedClothing.getNome() + " §ano pet " + currentPet.getNome() + "!");
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1.2f);
                    }

                    Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                        MenuManager.openWardrobeMenu(player, currentPet.getId());
                    });
                }
            }
            return;
        }

        if (title.startsWith("§8Comida: ")) {
            String petName = title.replace("§8Comida: ", "").trim();
            PetModel currentPet = PetRegistry.getByName(petName);
            if (currentPet == null) return;

            if (slot == 49) {
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), () -> {
                    MenuManager.openPetProfile(player, currentPet.getId());
                });
                return;
            }

            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                String cleanName = clickedItem.getItemMeta().getDisplayName().replaceAll("(?i)§[0-9A-FK-ORX]", "").trim();
                dev.gdataa.hyvel.pets.items.PetFood clickedFood = null;

                for (dev.gdataa.hyvel.pets.items.PetFood f : dev.gdataa.hyvel.pets.items.FoodRegistry.getAllFood()) {
                    if (f.getNome().replaceAll("(?i)§[0-9A-FK-ORX]", "").trim().equalsIgnoreCase(cleanName)) {
                        clickedFood = f;
                        break;
                    }
                }

                if (clickedFood != null) {
                    // Aplica o buff na DB (Converte minutos para milissegundos)
                    PetsMain.db().feedPet(player.getUniqueId(), currentPet.getId(), clickedFood.getId(), clickedFood.getDuracaoMinutos() * 60 * 1000L);

                    player.sendMessage("§a§lNHAM! §7O pet " + currentPet.getNome() + " §7comeu " + clickedFood.getNome() + " §7e ganhou bónus!");
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                    player.closeInventory();
                }
            }
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

    @EventHandler
    public void onPlayerChatBid(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (pendingBids.containsKey(playerUUID)) {
            event.setCancelled(true);
            String message = event.getMessage();

            if (message.equalsIgnoreCase("cancelar")) {
                pendingBids.remove(playerUUID);
                player.sendMessage("§cLance cancelado.");
                return;
            }

            UUID auctionId = pendingBids.get(playerUUID);
            dev.gdataa.hyvel.shops.AuctionItem targetAuction = null;

            for (dev.gdataa.hyvel.shops.AuctionItem a : dev.gdataa.hyvel.shops.AuctionManager.activeAuctions) {
                if (a.getAuctionId().equals(auctionId)) {
                    targetAuction = a;
                    break;
                }
            }

            if (targetAuction == null) {
                player.sendMessage("§cEste leilão já expirou ou foi removido!");
                pendingBids.remove(playerUUID);
                return;
            }

            try {
                double bidAmount = Double.parseDouble(message);
                String tipoMoeda = targetAuction.isCash() ? "cash" : "coins";

                // Variáveis para salvaguardar quem estava a ganhar antes da base de dados processar
                final dev.gdataa.hyvel.shops.AuctionItem finalTargetAuction = targetAuction;

                dev.gdataa.hyvel.shops.EconomyManager.processPurchase(player, tipoMoeda, bidAmount,
                        () -> {
                            // SUCESSO: O dinheiro foi tirado! Agora vamos tentar colocar o lance no leilão.
                            double lanceAnterior = finalTargetAuction.getCurrentBid();
                            UUID licitadorAnterior = finalTargetAuction.getHighestBidderUuid();

                            if (finalTargetAuction.placeBid(playerUUID, player.getName(), bidAmount)) {

                                // Se alguém estava a ganhar antes, devolvemos-lhe o dinheiro!
                                if (licitadorAnterior != null) {
                                    dev.gdataa.hyvel.shops.EconomyManager.addBalanceOffline(licitadorAnterior, tipoMoeda, lanceAnterior);
                                    Player pAnterior = Bukkit.getPlayer(licitadorAnterior);
                                    if (pAnterior != null) {
                                        pAnterior.sendMessage("§e§lLEILÃO! §7Alguém superou o teu lance! Os teus " + lanceAnterior + " " + tipoMoeda + " foram devolvidos.");
                                    }
                                }

                                player.sendMessage("§a§lLANCE ACEITE! §7És agora o maior licitador deste item!");
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                                pendingBids.remove(playerUUID);
                                Bukkit.broadcastMessage("§6[Leilão] §e" + player.getName() + " §7deu um lance de §f" + bidAmount + " " + tipoMoeda + " §7num item!");

                            } else {
                                // Cuidado! O jogador demorou muito a digitar e alguém já deu um lance maior!
                                // O dinheiro foi tirado, por isso temos de o devolver à pressa.
                                dev.gdataa.hyvel.shops.EconomyManager.addBalanceOffline(playerUUID, tipoMoeda, bidAmount);
                                player.sendMessage("§cAlguém deu um lance maior enquanto a tua transação era processada. O teu dinheiro foi devolvido!");
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            }
                        },
                        () -> {
                            // FALHA: Não tem saldo suficiente
                            player.sendMessage("§cNão tens saldo suficiente (" + tipoMoeda + ").");
                        }
                );

            } catch (NumberFormatException e) {
                player.sendMessage("§cPor favor, digita apenas números (ex: 5000) ou 'cancelar'.");
            }
        }
    }
}