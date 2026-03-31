package dev.gdataa.hyvel.shops;

import br.com.ystoreplugins.product.yeconomias.EconomiasAPIHolder;
import dev.gdataa.hyvel.PetsMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class EconomyManager {

    private static EconomiasAPIHolder econAPI;

    // Conecta à API do yEconomias (Chamar no onEnable da PetsMain)
    public static boolean setupEconomy() {
        try {
            RegisteredServiceProvider<EconomiasAPIHolder> rsp = Bukkit.getServer().getServicesManager().getRegistration(EconomiasAPIHolder.class);
            if (rsp == null) return false;
            econAPI = rsp.getProvider();
            return econAPI != null;
        } catch (Throwable t) {
            return false;
        }
    }

    // Processa uma compra de forma Assíncrona (não trava o servidor)
    public static void processPurchase(Player player, String currency, double amount, Runnable onSuccess, Runnable onFail) {
        if (econAPI == null) {
            onFail.run();
            return;
        }

        econAPI.getAccount(player.getName()).thenAccept(user -> {
            if (user == null) {
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), onFail);
                return;
            }

            double balance = econAPI.getBalance(user, currency);
            if (balance >= amount) {
                econAPI.removeBalance(user, currency, amount);
                // Retorna à thread principal para executar o sucesso (dar o item, mandar mensagem, etc)
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), onSuccess);
            } else {
                Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), onFail);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            Bukkit.getScheduler().runTask(org.bukkit.plugin.java.JavaPlugin.getPlugin(PetsMain.class), onFail);
            return null;
        });
    }

    // Adiciona dinheiro a um jogador (usado para pagar o vendedor no leilão ou devolver lances)
    public static void addBalanceOffline(UUID uuid, String currency, double amount) {
        if (econAPI == null) return;
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        if (playerName != null) {
            econAPI.getAccount(playerName).thenAccept(user -> {
                if (user != null) {
                    econAPI.addBalance(user, currency, amount);
                }
            });
        }
    }
}