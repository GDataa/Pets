package dev.gdataa.hyvel.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;

public class DatabaseManager {
    private Connection connection;

    public void setup(boolean useMySQL, String host, String db, String user, String pass) {
        try {
            if (useMySQL) {
                String url = "jdbc:mysql://" + host + "/" + db;
                connection = DriverManager.getConnection(url, user, pass);
            } else {
                java.io.File folder = org.bukkit.plugin.java.JavaPlugin.getPlugin(dev.gdataa.hyvel.PetsMain.class).getDataFolder();
                if (!folder.exists()) folder.mkdirs();

                java.io.File databaseFile = new java.io.File(folder, "database.db");

                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            }
            createTables();
        } catch (Exception e) {
            Bukkit.getLogger().severe("[HyvelPets] ERRO CRÍTICO: Não foi possível conectar ao banco de dados!");
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement s = connection.createStatement()) {
            // --- TABELAS DOS PETS ---
            s.execute("CREATE TABLE IF NOT EXISTS unlocked_pets (" +
                    "uuid VARCHAR(36), " +
                    "pet_id VARCHAR(32))");

            s.execute("CREATE TABLE IF NOT EXISTS equipped_pets (" +
                    "player_uuid VARCHAR(36), " +
                    "slot_index INT, " +
                    "pet_id VARCHAR(32), " +
                    "PRIMARY KEY (player_uuid, slot_index))");

            s.execute("CREATE TABLE IF NOT EXISTS visual_pets (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY, " +
                    "pet_id VARCHAR(32))");

            // --- TABELAS DAS ROUPAS (NOVO) ---
            s.execute("CREATE TABLE IF NOT EXISTS unlocked_clothing (" +
                    "uuid VARCHAR(36), " +
                    "clothing_id VARCHAR(32))");

            s.execute("CREATE TABLE IF NOT EXISTS equipped_clothing (" +
                    "player_uuid VARCHAR(36), " +
                    "pet_id VARCHAR(32), " +
                    "clothing_id VARCHAR(32), " +
                    "PRIMARY KEY (player_uuid, pet_id))");

            // Tabela de Comidas Ativas (Bónus Temporário)
            s.execute("CREATE TABLE IF NOT EXISTS active_food_buffs (" +
                    "player_uuid VARCHAR(36), " +
                    "pet_id VARCHAR(32), " +
                    "food_id VARCHAR(32), " +
                    "expire_time BIGINT, " +
                    "PRIMARY KEY (player_uuid, pet_id))");

            s.execute("CREATE TABLE IF NOT EXISTS unlocked_food (" +
                    "uuid VARCHAR(36), " +
                    "food_id VARCHAR(32))");
        }
    }

    // =========================================
    //             MÉTODOS DOS PETS
    // =========================================

    public List<String> getUnlockedPets(UUID uuid) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT pet_id FROM unlocked_pets WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("pet_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void unlockPet(UUID uuid, String petId) {
        String sql = "INSERT INTO unlocked_pets (uuid, pet_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, petId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getEquippedPet(UUID uuid, int slot) {
        String sql = "SELECT pet_id FROM equipped_pets WHERE player_uuid = ? AND slot_index = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, slot);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("pet_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void equipPet(UUID uuid, String petId, int slot) {
        if (petId == null) {
            String sql = "DELETE FROM equipped_pets WHERE player_uuid = ? AND slot_index = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, slot);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            String sql = "INSERT OR REPLACE INTO equipped_pets (player_uuid, slot_index, pet_id) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, slot);
                ps.setString(3, petId);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public String getVisualPet(UUID uuid) {
        String sql = "SELECT pet_id FROM visual_pets WHERE player_uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("pet_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void setVisualPet(UUID uuid, String petId) {
        if (petId == null) {
            String sql = "DELETE FROM visual_pets WHERE player_uuid = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            String sql = "INSERT OR REPLACE INTO visual_pets (player_uuid, pet_id) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, petId);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // =========================================
    //            MÉTODOS DAS ROUPAS
    // =========================================

    public List<String> getUnlockedClothing(UUID uuid) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT clothing_id FROM unlocked_clothing WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("clothing_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void unlockClothing(UUID uuid, String clothingId) {
        // Primeiro verifica se o jogador já tem esta roupa para não duplicar na base de dados
        if (getUnlockedClothing(uuid).contains(clothingId)) return;

        String sql = "INSERT INTO unlocked_clothing (uuid, clothing_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, clothingId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String getEquippedClothing(UUID uuid, String petId) {
        String sql = "SELECT clothing_id FROM equipped_clothing WHERE player_uuid = ? AND pet_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, petId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("clothing_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void equipClothing(UUID uuid, String petId, String clothingId) {
        if (clothingId == null) {
            String sql = "DELETE FROM equipped_clothing WHERE player_uuid = ? AND pet_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, petId);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        } else {
            String sql = "INSERT OR REPLACE INTO equipped_clothing (player_uuid, pet_id, clothing_id) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, petId);
                ps.setString(3, clothingId);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // =========================================
    //            MÉTODOS DAS COMIDAS (BUFFS)
    // =========================================

    public void feedPet(UUID uuid, String petId, String foodId, long durationMillis) {
        long expireTime = System.currentTimeMillis() + durationMillis;
        String sql = "INSERT OR REPLACE INTO active_food_buffs (player_uuid, pet_id, food_id, expire_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, petId);
            ps.setString(3, foodId);
            ps.setLong(4, expireTime);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Retorna o ID da comida ativa. Se já tiver expirado, limpa-a e retorna null.
    public String getActiveFoodBuff(UUID uuid, String petId) {
        String sql = "SELECT food_id, expire_time FROM active_food_buffs WHERE player_uuid = ? AND pet_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, petId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expireTime = rs.getLong("expire_time");
                if (System.currentTimeMillis() > expireTime) {
                    // O tempo acabou! Limpa da base de dados.
                    removeFoodBuff(uuid, petId);
                    return null;
                }
                return rs.getString("food_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void removeFoodBuff(UUID uuid, String petId) {
        String sql = "DELETE FROM active_food_buffs WHERE player_uuid = ? AND pet_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, petId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String> getUnlockedFood(UUID uuid) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT food_id FROM unlocked_food WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getString("food_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void unlockFood(UUID uuid, String foodId) {
        if (getUnlockedFood(uuid).contains(foodId)) return;
        String sql = "INSERT INTO unlocked_food (uuid, food_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, foodId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Connection getConnection() { return connection; }
}