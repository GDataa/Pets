package dev.gdataa.hyvel.pets.items;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class FoodRegistry {
    private static final List<PetFood> ALL_FOOD = new ArrayList<>();

    public static void loadFromConfig(FileConfiguration config) {
        ALL_FOOD.clear();

        ConfigurationSection section = config.getConfigurationSection("comidas");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String nome = section.getString(id + ".nome").replace("&", "§");
            String textura = section.getString(id + ".textura");
            String raridade = section.getString(id + ".raridade").replace("&", "§");
            int duracao = section.getInt(id + ".duracao_minutos", 30);
            double preco = section.getDouble(id + ".preco", 5000.0);

            List<String> bonus = new ArrayList<>();
            for (String b : section.getStringList(id + ".bonus")) {
                bonus.add(b.replace("&", "§"));
            }

            ALL_FOOD.add(new PetFood(id, nome, textura, raridade, bonus, duracao, preco));
        }
    }

    public static List<PetFood> getAllFood() {
        return new ArrayList<>(ALL_FOOD);
    }

    public static PetFood getById(String id) {
        return ALL_FOOD.stream().filter(f -> f.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
}