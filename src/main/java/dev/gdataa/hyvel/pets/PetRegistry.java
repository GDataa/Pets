package dev.gdataa.hyvel.pets;

import dev.gdataa.hyvel.menus.MenuManager;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PetRegistry {

    private static final List<PetModel> ALL_PETS = new ArrayList<>();

    // Centralizamos os Base64 aqui para não poluir o código do registro
    public static class Texturas {
        public static final String TOUPEIRA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjIyZmEzMGVjMDQxZTBhNmQzMzQ1ZDUyODM3NmU5MjA5N2RiOGE0MTBjNzBmMDljMDQzYThkOTlmYzI0YTIxMSJ9fX0=";
        public static final String ANAO_GELO = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5ZTAwYzViNmI0NzJjNTA0OGU0OTIwNTc0NzhjMDE0YjQ0NWVlNzNiZTZiNDY3Yjk0NjA1YzFjZDg0OGYzNSJ9fX0=";
        public static final String PORQUINHO = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjI1NWMzYmYzNWVkYzVhZTE0YjRkMzg0ODMxNWYzODhmZmJmOWM5ZmRlMjFiYjc5OTk5M2E4MzJmZmM2In19fQ==";
        public static final String ABELHA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkxMmM3NDMxYWJlZmFiNTJiOTM1ZTI4ZWUwYzE0MzhmZWI2MjdlMWQwODBiZGQ3NjllZjcyZjQ5OTQ4MjU4MSJ9fX0=";
        public static final String AXOLOTE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMxNTExNWYzOTAwODg3Mjc1NmE0ZmI3ZTVlNmUxZDU0YmM5YTBkZTgzMDIzODI0NGM4ZjQ2ZWVjMzVjZTM5ZSJ9fX0=";
    }

    static {
        register(new PetModel("MINER_MOLE", "§6Toupeira Escavadora", Texturas.TOUPEIRA,
                PetCategory.MINERACAO, "§7Comum", Arrays.asList("§7- §f10% chance de Double Drop")));

        register(new PetModel("MINER_DWARF", "§bAnão de Gelo", Texturas.ANAO_GELO,
                PetCategory.MINERACAO, "§9Raro", Arrays.asList("§7- §fEfeito de Pressa (Haste II)")));

        register(new PetModel("ECON_PIGGY", "§dPorquinho de Ouro", Texturas.PORQUINHO,
                PetCategory.ECONOMIA, "§eÉpico", Arrays.asList("§7- §fMultiplicador de Money: 1.2x")));

        register(new PetModel("FARM_BEE", "§eAbelha Polinizadora", Texturas.ABELHA,
                PetCategory.FARM, "§7Comum", Arrays.asList("§7- §fCrescimento instantâneo")));

        register(new PetModel("FISH_AXOLOTL", "§dAxolote Guardião", Texturas.AXOLOTE,
                PetCategory.PESCA, "§6Lendário", Arrays.asList("§7- §fTesouros na pesca: +15%")));
    }

    private static void register(PetModel pet) {
        ALL_PETS.add(pet);
    }

    public static List<PetModel> getAllPets() {
        return new ArrayList<>(ALL_PETS);
    }

    public static ItemStack createPetConsumable(PetModel pet) {
        List<String> lore = new ArrayList<>();
        lore.add(pet.getRaridade());
        lore.add("");
        lore.addAll(pet.getBonus());
        lore.add("");
        lore.add("§a§lCLIQUE DIREITO");
        lore.add("§7Para adicionar à sua coleção.");

        return MenuManager.createCustomHead(pet.getTextura(), "§6Pet: " + pet.getNome(), lore.toArray(new String[0]));
    }

    public static PetModel getById(String id) {
        return ALL_PETS.stream().filter(p -> p.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public static PetModel getByName(String displayName) {
        // Remove o prefixo "§6Pet: " e qualquer código de cor § seguido de um caractere
        String cleanName = displayName.replace("§6Pet: ", "")
                .replaceAll("(?i)§[0-9A-FK-ORX]", "")
                .trim();

        for (PetModel pet : getAllPets()) {
            String cleanPetName = pet.getNome().replaceAll("(?i)§[0-9A-FK-ORX]", "").trim();
            if (cleanPetName.equalsIgnoreCase(cleanName)) {
                return pet;
            }
        }
        return null;
    }
}