package dev.gdataa.hyvel.menus;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.pets.PetModel;
import dev.gdataa.hyvel.pets.PetRegistry;
import dev.gdataa.hyvel.pets.items.ClothingRegistry;
import dev.gdataa.hyvel.pets.items.PetClothing;
import dev.gdataa.hyvel.pets.items.PetFood;
import dev.gdataa.hyvel.shops.ShopManager;
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

    public static void openPetShopMenu(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "§8Loja de Pets");

        gui.setItem(11, createItem(Material.LEATHER_CHESTPLATE, "§bRoupas e Acessórios",
                "§7Compre itens estéticos que fornecem",
                "§7bónus §epermanentes §7aos seus pets.",
                "",
                "§eClique para abrir!"));

        gui.setItem(13, createItem(Material.GOLDEN_APPLE, "§eComida e Petiscos",
                "§7Alimente os seus pets para ativar",
                "§7multiplicadores de status §etemporários§7.",
                "",
                "§eClique para abrir!"));

        gui.setItem(15, createItem(Material.GOLD_INGOT, "§6Leilão de Pets",
                "§7Compre e venda itens raros ou",
                "§7pets com outros jogadores.",
                "",
                "§eClique para abrir!"));

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(22, createCustomHead(voltarValue, "§cVoltar", "§7Voltar ao Menu Principal."));

        player.openInventory(gui);
    }

    public static void openEvolutionSelector(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "§8Escolha um Pet para Evoluir");

        int[] slots = {10, 12, 14, 16};
        boolean hasPets = false;

        for (int i = 0; i < 4; i++) {
            String equippedPetId = PetsMain.db().getEquippedPet(player.getUniqueId(), i);
            if (equippedPetId != null) {
                PetModel pet = PetRegistry.getById(equippedPetId);
                if (pet != null) {
                    gui.setItem(slots[i], createCustomHead(pet.getTextura(), "§a" + pet.getNome(),
                            "§7Nível: §f1",
                            "",
                            "§eClique para gerir este pet!"));
                    hasPets = true;
                }
            }
        }

        if (!hasPets) {
            gui.setItem(13, createItem(Material.BARRIER, "§cNenhum Pet Equipado", "§7Equipe um pet no menu 'Meus Pets'", "§7antes de tentar evoluí-lo."));
        }

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(22, createCustomHead(voltarValue, "§cVoltar", "§7Voltar ao Menu Principal."));

        player.openInventory(gui);
    }

    public static void openPetProfile(Player player, String petId) {
        PetModel pet = PetRegistry.getById(petId);
        if (pet == null) return;

        Inventory gui = Bukkit.createInventory(player, 27, "§8Gerir: " + pet.getNome());

        List<String> statsLore = new ArrayList<>();
        statsLore.add(pet.getRaridade());
        statsLore.add("");
        statsLore.addAll(pet.getBonus());
        statsLore.add("");
        String activeFoodId = PetsMain.db().getActiveFoodBuff(player.getUniqueId(), petId);
        if (activeFoodId != null) {
            dev.gdataa.hyvel.pets.items.PetFood food = dev.gdataa.hyvel.pets.items.FoodRegistry.getById(activeFoodId);
            statsLore.add("§bEfeito Ativo: §e" + (food != null ? food.getNome() : "Desconhecido"));
        } else {
            statsLore.add("§bEfeito Ativo: §7Nenhum");
        }
        gui.setItem(13, createCustomHead(pet.getTextura(), "§6" + pet.getNome(), statsLore.toArray(new String[0])));

        gui.setItem(11, createItem(Material.GOLDEN_APPLE, "§eAlimentar Pet",
                "§7Dê comidas especiais para ativar",
                "§7bónus temporários absurdos!",
                "",
                "§eClique para abrir a mochila de comida"));

        String equippedClothingId = PetsMain.db().getEquippedClothing(player.getUniqueId(), petId);
        if (equippedClothingId != null) {
            PetClothing clothing = ClothingRegistry.getById(equippedClothingId);
            if (clothing != null) {
                gui.setItem(15, createCustomHead(clothing.getTexturaBase64(), "§bGuarda-Roupa",
                        "§7Roupa Atual: §a" + clothing.getNome(),
                        "",
                        "§eClique para alterar ou desequipar"));
            }
        } else {
            gui.setItem(15, createItem(Material.LEATHER_CHESTPLATE, "§bGuarda-Roupa",
                    "§7Equipe itens permanentes para",
                    "§7aumentar os status base do pet.",
                    "",
                    "§cNenhuma roupa equipada.",
                    "",
                    "§eClique para equipar algo"));
        }

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(22, createCustomHead(voltarValue, "§cVoltar", "§7Voltar à lista de pets."));

        player.openInventory(gui);
    }

    public static void openWardrobeMenu(Player player, String petId) {
        PetModel pet = PetRegistry.getById(petId);
        if (pet == null) return;

        Inventory gui = Bukkit.createInventory(player, 54, "§8Guarda-Roupa: " + pet.getNome());

        List<String> unlockedClothing = PetsMain.db().getUnlockedClothing(player.getUniqueId());
        String equippedClothingId = PetsMain.db().getEquippedClothing(player.getUniqueId(), petId);

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (String clothingId : unlockedClothing) {
            if (index >= slots.length) break;
            PetClothing clothing = ClothingRegistry.getById(clothingId);
            if (clothing != null) {
                List<String> lore = new ArrayList<>();
                lore.add(clothing.getRaridade());
                lore.add("");
                lore.addAll(clothing.getBonusExtra());
                lore.add("");

                if (clothing.getId().equals(equippedClothingId)) {
                    lore.add("§a§lEQUIPADA");
                    lore.add("§cClique para desequipar.");
                } else {
                    lore.add("§eClique para equipar!");
                }

                gui.setItem(slots[index], createCustomHead(clothing.getTexturaBase64(), "§b" + clothing.getNome(), lore.toArray(new String[0])));
                index++;
            }
        }

        if (unlockedClothing.isEmpty()) {
            gui.setItem(22, createItem(Material.BARRIER, "§cGuarda-Roupa Vazio", "§7Você não comprou nenhuma roupa", "§7na Loja de Pets ainda!"));
        }

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(49, createCustomHead(voltarValue, "§cVoltar", "§7Voltar ao perfil do pet."));

        player.openInventory(gui);
    }

    public static void openFoodBackpack(Player player, String petId) {
        PetModel pet = PetRegistry.getById(petId);
        if (pet == null) return;

        Inventory gui = Bukkit.createInventory(player, 54, "§8Comida: " + pet.getNome());

        List<String> unlockedFood = PetsMain.db().getUnlockedFood(player.getUniqueId());
        String activeFoodId = PetsMain.db().getActiveFoodBuff(player.getUniqueId(), petId);

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int index = 0;
        for (String foodId : unlockedFood) {
            if (index >= slots.length) break;
            dev.gdataa.hyvel.pets.items.PetFood food = dev.gdataa.hyvel.pets.items.FoodRegistry.getById(foodId);
            if (food != null) {
                List<String> lore = new ArrayList<>();
                lore.add(food.getRaridade());
                lore.add("");
                lore.addAll(food.getBonusTemporario());
                lore.add("");
                lore.add("§7Duração: §b" + food.getDuracaoMinutos() + "m");
                lore.add("");

                if (food.getId().equals(activeFoodId)) {
                    lore.add("§a§lALIMENTO ATIVO");
                    lore.add("§7O pet está a digerir isto...");
                } else {
                    lore.add("§eClique para alimentar!");
                }

                gui.setItem(slots[index], createCustomHead(food.getTexturaBase64(), "§e" + food.getNome(), lore.toArray(new String[0])));
                index++;
            }
        }

        if (unlockedFood.isEmpty()) {
            gui.setItem(22, createItem(Material.BARRIER, "§cMochila Vazia", "§7Compre comidas na Loja de Pets!"));
        }

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(49, createCustomHead(voltarValue, "§cVoltar", "§7Voltar ao perfil do pet."));

        player.openInventory(gui);
    }

    // Slots centrais perfeitos para a montra do leilão
    public static final int[] AUCTION_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    // === MENU DO LEILÃO (PAGINADO) ===
    public static void openAuctionMenu(Player player, int page) {
        Inventory gui = Bukkit.createInventory(player, 54, "§8Leilão Ativo - Pág. " + page);

        List<dev.gdataa.hyvel.shops.AuctionItem> auctions = dev.gdataa.hyvel.shops.AuctionManager.activeAuctions;

        // Calcula o total de páginas
        int totalPages = (int) Math.ceil((double) auctions.size() / AUCTION_SLOTS.length);
        if (totalPages == 0) totalPages = 1;

        // Segurança para não quebrar a página
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;

        int startIndex = (page - 1) * AUCTION_SLOTS.length;

        // Preenche os slots disponíveis com os itens daquela página
        for (int i = 0; i < AUCTION_SLOTS.length; i++) {
            int auctionIndex = startIndex + i;
            if (auctionIndex < auctions.size()) {
                gui.setItem(AUCTION_SLOTS[i], auctions.get(auctionIndex).getDisplayItem());
            }
        }

        if (auctions.isEmpty()) {
            gui.setItem(22, createItem(Material.BARRIER, "§cLeilão Vazio", "§7Nenhum jogador está a leiloar", "§7itens neste momento."));
        }

        // ===================================
        //        SISTEMA DE PAGINAÇÃO
        // ===================================
        if (page > 1) {
            String prevHead = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmM2ZWI2YmJiMWRiNGExMjEzYzU4ZWNkNDk1MTdlMjk2ZDM5OGM1Mjg2YWU0N2RmNWVlZTNmZWU2NjM4ZSJ9fX0=";
            gui.setItem(27, createCustomHead(prevHead, "§ePágina Anterior", "§7Ir para a página " + (page - 1)));
        }

        if (page < totalPages) {
            String nextHead = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjk0NmEzMDlkMWU5ZTYxYzg4NTI4MjMxZTk2NWZkNDg2YmNjNDcxYjE1OWI3MzhjODg5ZmQ3NWY0ZjFiZmRlNCJ9fX0=";
            gui.setItem(35, createCustomHead(nextHead, "§ePróxima Página", "§7Ir para a página " + (page + 1)));
        }

        // ===================================

        String infoValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAxYWZlOTczYzU0ODJmZjc0MjZmYzhlZTkzMjcwNTY2MWRiN2NlZmI1NjQ1YjY5YjU2ZWY5M2VkZWZmZGIzIn19fQ==";
        gui.setItem(48, createCustomHead(infoValue, "§bComo funciona?",
                "§7Para leiloar um item que tenhas na mão,",
                "§7utiliza o comando:",
                "§f/pets leilao <lance> <tempo> <coins/cash>"));

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(49, createCustomHead(voltarValue, "§cVoltar", "§7Voltar à Loja."));

        player.openInventory(gui);
    }

    public static void openClothingShop(Player player) {
        Inventory gui = Bukkit.createInventory(player, 45, "§8Loja: Roupas");

        List<PetClothing> roupas = ShopManager.roupasAtuais;
        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < roupas.size(); i++) {
            if (i >= slots.length) break;
            PetClothing roupa = roupas.get(i);

            List<String> lore = new ArrayList<>();
            lore.add(roupa.getRaridade());
            lore.add("");
            lore.addAll(roupa.getBonusExtra());
            lore.add("");
            lore.add("§7Preço: §6" + roupa.getPreco() + " Coins");
            lore.add("");

            if (PetsMain.db().getUnlockedClothing(player.getUniqueId()).contains(roupa.getId())) {
                lore.add("§cVocê já possui esta roupa!");
            } else {
                lore.add("§a§lCLIQUE PARA COMPRAR");
            }

            gui.setItem(slots[i], createCustomHead(roupa.getTexturaBase64(), roupa.getNome(), lore.toArray(new String[0])));
        }

        gui.setItem(40, createItem(Material.CLOCK, "§ePróxima Rotação",
                "§7A loja atualiza os seus itens em:",
                "§f" + ShopManager.getTempoRestanteFormatado()));

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(39, createCustomHead(voltarValue, "§cVoltar", "§7Voltar às categorias."));

        player.openInventory(gui);
    }

    public static void openFoodShop(Player player) {
        Inventory gui = Bukkit.createInventory(player, 45, "§8Loja: Comidas");

        List<PetFood> comidas = ShopManager.comidasAtuais;
        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < comidas.size(); i++) {
            if (i >= slots.length) break;
            PetFood comida = comidas.get(i);

            List<String> lore = new ArrayList<>();
            lore.add(comida.getRaridade());
            lore.add("");
            lore.addAll(comida.getBonusTemporario());
            lore.add("");
            lore.add("§7Duração: §b" + comida.getDuracaoMinutos() + " minutos");
            lore.add("§7Preço: §6" + comida.getPreco() + " Coins");
            lore.add("");
            lore.add("§a§lCLIQUE PARA COMPRAR");

            gui.setItem(slots[i], createCustomHead(comida.getTexturaBase64(), comida.getNome(), lore.toArray(new String[0])));
        }

        gui.setItem(40, createItem(Material.CLOCK, "§ePróxima Rotação",
                "§7A loja atualiza os seus itens em:",
                "§f" + ShopManager.getTempoRestanteFormatado()));

        String voltarValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZhOGQyOTIxOWUyYzljODQ5NTQ2OTNiODdmMzdhZWNlM2MyNzFkNTdmYjZhMmQ3MWZhZmUzOWYwYjgwNDAifX19";
        gui.setItem(39, createCustomHead(voltarValue, "§cVoltar", "§7Voltar às categorias."));

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
                Bukkit.getLogger().warning("Erro ao carregar textura Base64 para o item: " + name);
            }

            meta.setOwnerProfile(profile);
            head.setItemMeta(meta);
        }
        return head;
    }
}