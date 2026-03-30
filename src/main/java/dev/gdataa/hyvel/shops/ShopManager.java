package dev.gdataa.hyvel.shops;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.pets.items.ClothingRegistry;
import dev.gdataa.hyvel.pets.items.FoodRegistry;
import dev.gdataa.hyvel.pets.items.PetClothing;
import dev.gdataa.hyvel.pets.items.PetFood;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopManager {

    // Itens que estão atualmente na montra da loja
    public static List<PetClothing> roupasAtuais = new ArrayList<>();
    public static List<PetFood> comidasAtuais = new ArrayList<>();

    // Tempo em que a loja vai rodar novamente (em milissegundos)
    public static long proximaRotacaoMillis = 0;

    public static void iniciarRotacao(PetsMain plugin) {
        plugin.saveDefaultConfig(); // Garante que a config.yml existe

        int minutos = plugin.getConfig().getInt("loja.tempo_rotacao_minutos", 60);

        // Faz a primeira rotação imediatamente ao ligar o servidor
        rodarItens(plugin);

        // Inicia a Task que vai rodar de X em X minutos
        new BukkitRunnable() {
            @Override
            public void run() {
                rodarItens(plugin);
            }
        }.runTaskTimer(plugin, minutos * 60 * 20L, minutos * 60 * 20L); // Converte minutos para ticks
    }

    private static void rodarItens(PetsMain plugin) {
        int qtdRoupas = plugin.getConfig().getInt("loja.quantidade_roupas_venda", 3);
        int qtdComidas = plugin.getConfig().getInt("loja.quantidade_comidas_venda", 5);

        // 1. Atualiza o relógio da próxima rotação
        int minutos = plugin.getConfig().getInt("loja.tempo_rotacao_minutos", 60);
        proximaRotacaoMillis = System.currentTimeMillis() + (minutos * 60 * 1000L);

        // 2. Baralha e escolhe as Roupas
        List<PetClothing> todasAsRoupas = ClothingRegistry.getAllClothing();
        Collections.shuffle(todasAsRoupas); // Mistura a lista aleatoriamente
        roupasAtuais.clear();
        for (int i = 0; i < Math.min(qtdRoupas, todasAsRoupas.size()); i++) {
            roupasAtuais.add(todasAsRoupas.get(i));
        }

        // 3. Baralha e escolhe as Comidas
        List<PetFood> todasAsComidas = FoodRegistry.getAllFood();
        Collections.shuffle(todasAsComidas);
        comidasAtuais.clear();
        for (int i = 0; i < Math.min(qtdComidas, todasAsComidas.size()); i++) {
            comidasAtuais.add(todasAsComidas.get(i));
        }

        Bukkit.getLogger().info("[HyvelPets] A loja rotativa foi atualizada com novos itens!");
    }

    // Método útil para formatar o tempo restante e mostrar no Menu da Loja
    public static String getTempoRestanteFormatado() {
        long faltam = proximaRotacaoMillis - System.currentTimeMillis();
        if (faltam <= 0) return "Atualizando...";

        long minutos = (faltam / 1000) / 60;
        long segundos = (faltam / 1000) % 60;
        return String.format("%02dm %02ds", minutos, segundos);
    }
}