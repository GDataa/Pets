package dev.gdataa.hyvel.listeners;

import dev.gdataa.hyvel.pets.PetBonusManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class PetMechanicsListener implements Listener {

    // APLICAR O BÓNUS DE DANO (Exemplo: "+15% de Dano Físico")
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            // Procura a palavra "Dano" em todas as lores dos pets equipados/roupas/comidas
            double bonusDanoPercentual = PetBonusManager.getTotalBonus(player, "Dano");

            if (bonusDanoPercentual > 0) {
                double danoOriginal = event.getDamage();
                double danoExtra = danoOriginal * (bonusDanoPercentual / 100.0);

                // Aplica o novo dano final!
                event.setDamage(danoOriginal + danoExtra);
            }
        }
    }

    // APLICAR O BÓNUS DE XP (Exemplo: "+50% de XP ganho")
    @EventHandler
    public void onPlayerGainXP(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();

        // Procura a palavra "XP"
        double bonusXpPercentual = PetBonusManager.getTotalBonus(player, "XP");

        if (bonusXpPercentual > 0) {
            int xpOriginal = event.getAmount();
            int xpExtra = (int) (xpOriginal * (bonusXpPercentual / 100.0));

            event.setAmount(xpOriginal + xpExtra);
        }
    }
}