package dev.gdataa.hyvel.shops;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionItem {
    private final UUID auctionId;
    private final UUID sellerUuid;
    private final String sellerName;
    private final ItemStack originalItem;

    private final double startingBid;
    private double currentBid;

    private UUID highestBidderUuid;
    private String highestBidderName;

    private final long expireTimeMillis;
    private final boolean isCash; // true = Cash (✪), false = Coins ($)

    private static final DecimalFormat df = new DecimalFormat("#,###.##");

    public AuctionItem(UUID sellerUuid, String sellerName, ItemStack item, double startingBid, long durationMinutos, boolean isCash) {
        this.auctionId = UUID.randomUUID();
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        this.originalItem = item.clone();
        this.startingBid = startingBid;
        this.currentBid = 0; // Ainda não há lances
        this.expireTimeMillis = System.currentTimeMillis() + (durationMinutos * 60 * 1000L);
        this.isCash = isCash;
    }

    // --- LÓGICA DO LANCE ---
    public boolean placeBid(UUID bidderUuid, String bidderName, double amount) {
        // O lance tem de ser maior que o lance inicial e maior que o último lance
        double minimumBid = (currentBid == 0) ? startingBid : currentBid + 1; // +1 é o incremento mínimo (podes mudar)

        if (amount >= minimumBid) {
            this.highestBidderUuid = bidderUuid;
            this.highestBidderName = bidderName;
            this.currentBid = amount;
            return true;
        }
        return false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expireTimeMillis;
    }

    // --- DESENHAR O ITEM NO MENU ---
    public ItemStack getDisplayItem() {
        ItemStack display = originalItem.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add("§8§m------------------------");
            lore.add("§7Vendedor: §f" + sellerName);
            lore.add("§7Tempo Restante: §e" + getFormattedTimeLeft());
            lore.add("");

            String moeda = isCash ? "§6✪" : "§2$";
            lore.add("§7Lance Inicial: §f" + df.format(startingBid) + " " + moeda);

            if (currentBid == 0) {
                lore.add("§7Último Lance: §cNenhum lance!");
            } else {
                lore.add("§7Último Lance: §a" + highestBidderName + " §8(§f" + df.format(currentBid) + " " + moeda + "§8)");
            }

            lore.add("§8§m------------------------");
            lore.add("");
            lore.add("§a§lCLIQUE ESQUERDO §7para dar um lance!");
            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }

    private String getFormattedTimeLeft() {
        long faltam = expireTimeMillis - System.currentTimeMillis();
        if (faltam <= 0) return "§cExpirado";

        long horas = (faltam / 1000) / 3600;
        long minutos = ((faltam / 1000) % 3600) / 60;
        long segundos = (faltam / 1000) % 60;

        if (horas > 0) return String.format("%02dh %02dm %02ds", horas, minutos, segundos);
        return String.format("%02dm %02ds", minutos, segundos);
    }

    // Getters básicos
    public UUID getAuctionId() { return auctionId; }
    public UUID getSellerUuid() { return sellerUuid; }
    public String getSellerName() { return sellerName; } // <--- O MÉTODO QUE FALTAVA
    public ItemStack getOriginalItem() { return originalItem; }
    public double getCurrentBid() { return currentBid; }
    public UUID getHighestBidderUuid() { return highestBidderUuid; }
    public boolean isCash() { return isCash; }
}