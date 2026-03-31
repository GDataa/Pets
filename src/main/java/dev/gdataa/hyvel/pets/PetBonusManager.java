package dev.gdataa.hyvel.pets;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.pets.items.ClothingRegistry;
import dev.gdataa.hyvel.pets.items.FoodRegistry;
import dev.gdataa.hyvel.pets.items.PetClothing;
import dev.gdataa.hyvel.pets.items.PetFood;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetBonusManager {

    /**
     * Calcula o bónus total de um jogador com base numa palavra-chave.
     * Exemplo: getTotalBonus(player, "Dano") vai somar tudo o que for "+X% de Dano".
     */
    public static double getTotalBonus(Player player, String keyword) {
        double totalValue = 0.0;
        UUID uuid = player.getUniqueId();

        // Passa por todos os 4 slots possíveis do jogador
        for (int i = 0; i < 4; i++) {
            String equippedPetId = PetsMain.db().getEquippedPet(uuid, i);
            if (equippedPetId == null) continue;

            PetModel pet = PetRegistry.getById(equippedPetId);
            if (pet != null) {
                // 1. Soma os bónus base do Pet
                totalValue += extractValueFromLore(pet.getBonus(), keyword);

                // 2. Soma os bónus da Roupa (Permanente)
                String clothingId = PetsMain.db().getEquippedClothing(uuid, pet.getId());
                if (clothingId != null) {
                    PetClothing clothing = ClothingRegistry.getById(clothingId);
                    if (clothing != null) {
                        totalValue += extractValueFromLore(clothing.getBonusExtra(), keyword);
                    }
                }

                // 3. Soma os bónus da Comida (Temporário)
                String foodId = PetsMain.db().getActiveFoodBuff(uuid, pet.getId());
                if (foodId != null) {
                    PetFood food = FoodRegistry.getById(foodId);
                    if (food != null) {
                        totalValue += extractValueFromLore(food.getBonusTemporario(), keyword);
                    }
                }
            }
        }

        return totalValue;
    }

    /**
     * Procura numa lista de textos (lore) pela palavra-chave e extrai o número.
     * Ex: ["+15% de Dano Físico"] com keyword "Dano" retorna 15.0
     */
    private static double extractValueFromLore(List<String> lore, String keyword) {
        double sum = 0.0;
        // Padrão Regex para encontrar números (positivos ou negativos, com ou sem decimais)
        Pattern pattern = Pattern.compile("([+-]?\\d*\\.?\\d+)");

        for (String line : lore) {
            // Remove as cores (ex: §a) para não atrapalhar a leitura
            String cleanLine = line.replaceAll("(?i)§[0-9A-FK-ORX]", "").toLowerCase();

            // Se a linha contiver a palavra-chave (ex: "dano")
            if (cleanLine.contains(keyword.toLowerCase())) {
                Matcher matcher = pattern.matcher(cleanLine);
                if (matcher.find()) {
                    try {
                        sum += Double.parseDouble(matcher.group(1));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return sum;
    }
}