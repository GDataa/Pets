package dev.gdataa.hyvel.shops;

import dev.gdataa.hyvel.PetsMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AuctionManager {

    // Lista de leilões a decorrer neste momento
    public static final List<AuctionItem> activeAuctions = new ArrayList<>();

    public static void startAuctionTask(PetsMain plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Usamos o Iterator para poder remover itens da lista enquanto a lemos
                Iterator<AuctionItem> iterator = activeAuctions.iterator();

                while (iterator.hasNext()) {
                    AuctionItem auction = iterator.next();

                    if (auction.isExpired()) {
                        processAuctionEnd(auction);
                        iterator.remove(); // Remove o leilão da lista de ativos
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Roda a cada segundo (20 ticks)
    }

    public static void addAuction(AuctionItem auction) {
        activeAuctions.add(auction);
    }

    private static void processAuctionEnd(AuctionItem auction) {
        // Se alguém deu um lance
        if (auction.getCurrentBid() > 0 && auction.getHighestBidderUuid() != null) {

            // TODO: Entregar o dinheiro (Coins ou Cash) ao Vendedor (auction.getSellerUuid())

            // TODO: Entregar o Item ao Comprador (auction.getHighestBidderUuid())
            // Se o comprador estiver offline, temos de guardar o item numa base de dados ("Caixa de Correio/Entregas")

            Bukkit.getLogger().info("[HyvelPets] Leilão terminado: Item vendido!");

            Player winner = Bukkit.getPlayer(auction.getHighestBidderUuid());
            if (winner != null) {
                winner.sendMessage("§a§lLEILÃO! §7Ganhaste o leilão! O item foi enviado para o teu inventário.");
            }

            Player seller = Bukkit.getPlayer(auction.getSellerUuid());
            if (seller != null) {
                seller.sendMessage("§a§lLEILÃO! §7O teu item foi vendido por " + auction.getCurrentBid() + "!");
            }

        } else {
            // Ninguém deu lance - Devolver o item ao vendedor
            Bukkit.getLogger().info("[HyvelPets] Leilão terminado: Ninguém comprou.");

            // TODO: Devolver o item ao Vendedor
            Player seller = Bukkit.getPlayer(auction.getSellerUuid());
            if (seller != null) {
                seller.sendMessage("§c§lLEILÃO! §7O tempo acabou e ninguém deu um lance no teu item. Ele foi devolvido!");
                seller.getInventory().addItem(auction.getOriginalItem());
            } else {
                // Se estiver offline, também tem de ir para a tal "Caixa de Entregas" na DB
            }
        }
    }
}