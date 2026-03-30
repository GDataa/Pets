package dev.gdataa.hyvel.pets.items;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ClothingRegistry {
    private static final List<PetClothing> ALL_CLOTHING = new ArrayList<>();

    public static void loadFromConfig(FileConfiguration config) {
        ALL_CLOTHING.clear(); // Limpa a lista em caso de /reload

        ConfigurationSection section = config.getConfigurationSection("roupas");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String nome = section.getString(id + ".nome").replace("&", "§");
            String textura = section.getString(id + ".textura");
            String raridade = section.getString(id + ".raridade").replace("&", "§");
            double preco = section.getDouble(id + ".preco", 5000.0);

            List<String> bonus = new ArrayList<>();
            for (String b : section.getStringList(id + ".bonus")) {
                bonus.add(b.replace("&", "§"));
            }

            ALL_CLOTHING.add(new PetClothing(id, nome, textura, raridade, bonus, preco));
        }
    }

    public static List<PetClothing> getAllClothing() {
        return new ArrayList<>(ALL_CLOTHING);
    }

    public static PetClothing getById(String id) {
        return ALL_CLOTHING.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
}