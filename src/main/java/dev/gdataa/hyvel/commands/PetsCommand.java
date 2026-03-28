package dev.gdataa.hyvel.commands;

import dev.gdataa.hyvel.PetsMain;
import dev.gdataa.hyvel.menus.MenuManager;
import dev.gdataa.hyvel.pets.PetModel;
import dev.gdataa.hyvel.pets.PetRegistry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PetsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Comando base: /pets
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cApenas jogadores podem abrir o menu.");
                return true;
            }
            MenuManager.openMainMenu((Player) sender);
            return true;
        }

        // --- SUBCOMANDO: GIVE ---
        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("hyvel.pets.admin")) {
                sender.sendMessage("§cVocê não tem permissão.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("§cUso correto: /pets give <jogador> <pet_id>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cJogador não encontrado.");
                return true;
            }

            PetModel pet = PetRegistry.getById(args[2].toUpperCase());
            if (pet == null) {
                sender.sendMessage("§cID de pet inválido.");
                return true;
            }

            target.getInventory().addItem(PetRegistry.createPetConsumable(pet));
            sender.sendMessage("§aPet " + pet.getNome() + " §aentregue a " + target.getName() + ".");
            return true;
        }

        // --- SUBCOMANDO: RESET ---
        if (args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("hyvel.pets.admin")) {
                sender.sendMessage("§cVocê não tem permissão.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("§cUso correto: /pets reset <jogador>");
                return true;
            }

            // Usamos OfflinePlayer para poder resetar mesmo que o jogador não esteja no servidor
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UUID uuid = target.getUniqueId();

            // 1. Limpa os slots equipados (0 a 3)
            for (int i = 0; i < 4; i++) {
                PetsMain.db().equipPet(uuid, null, i);
            }

            // 2. Limpa a coleção (unlocked_pets)
            // Precisamos de um método no DatabaseManager para isso
            resetUnlockedPets(uuid);

            sender.sendMessage("§e[!] Todos os dados de pets de §f" + args[1] + " §eforam resetados.");

            // Se o jogador estiver online, fecha o inventário dele para evitar bugs visuais
            if (target.isOnline()) {
                ((Player) target).closeInventory();
                target.getPlayer().sendMessage("§cSeus dados de pets foram resetados por um administrador.");
            }
            return true;
        }

        sender.sendMessage("§cComando desconhecido.");
        return true;
    }

    // Método auxiliar para limpar a coleção no banco
    private void resetUnlockedPets(UUID uuid) {
        try {
            java.sql.PreparedStatement ps = PetsMain.db().getConnection().prepareStatement("DELETE FROM unlocked_pets WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("hyvel.pets.admin")) return completions;

        if (args.length == 1) {
            completions.add("give");
            completions.add("reset");
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (PetModel pet : PetRegistry.getAllPets()) completions.add(pet.getId());
        }
        return completions;
    }
}