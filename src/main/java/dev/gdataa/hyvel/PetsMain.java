package dev.gdataa.hyvel;

import dev.gdataa.hyvel.commands.PetsCommand;
import dev.gdataa.hyvel.database.DatabaseManager;
import dev.gdataa.hyvel.listeners.PetsGUIListener;
import dev.gdataa.hyvel.pets.PetVisualManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class PetsMain extends JavaPlugin {

    private static DatabaseManager dbManager;

    @Override
    public void onEnable() {

        // CRIA A PASTA DO PLUGIN CASO NÃO EXISTA
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        dbManager = new DatabaseManager();
        // Exemplo: desativado o MySQL, ele usará SQLite automaticamente
        dbManager.setup(false, "", "", "", "");

        if (dbManager.getConnection() == null) {
            Bukkit.getConsoleSender().sendMessage("§c[HyvelPets] DESATIVANDO: Banco de dados offline.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        PetsCommand petsCmd = new PetsCommand();
        getCommand("pets").setExecutor(petsCmd);
        getCommand("pets").setTabCompleter(petsCmd);
        getServer().getPluginManager().registerEvents(new PetsGUIListener(), this);

        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage("§a[HyvelPets] Plugin iniciado com sucesso!");
        Bukkit.getConsoleSender().sendMessage("§a[HyvelPets] Versão: 1.21.4");
        Bukkit.getConsoleSender().sendMessage(" ");

        PetVisualManager.startFollowTask(this);
    }

    public static DatabaseManager db() { return dbManager; }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§c[HyvelPets] Plugin desativado.");

        PetVisualManager.despawnAll();
    }
}