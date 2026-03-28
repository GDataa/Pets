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
            s.execute("CREATE TABLE IF NOT EXISTS unlocked_pets (" +
                    "uuid VARCHAR(36), " +
                    "pet_id VARCHAR(32))");

            s.execute("CREATE TABLE IF NOT EXISTS equipped_pets (" +
                    "player_uuid VARCHAR(36), " +
                    "slot_index INT, " +
                    "pet_id VARCHAR(32), " +
                    "PRIMARY KEY (player_uuid, slot_index))");

            // NOVA TABELA: Pet Visual
            s.execute("CREATE TABLE IF NOT EXISTS visual_pets (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY, " +
                    "pet_id VARCHAR(32))");
        }
    }

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

    // --- MÉTODOS DO PET VISUAL ---
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

    public Connection getConnection() { return connection; }
}