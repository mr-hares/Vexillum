package ru.mr_hares.vexillum.database;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import ru.mr_hares.vexillum.objects.CityDetails;
import ru.mr_hares.vexillum.objects.PeopleDetails;

import java.sql.*;
import java.util.*;

public class DataBase implements DataVoid {
    private Connection connection;
    private Plugin plugin;

    public DataBase(Plugin plugin) throws SQLException {
        this.plugin = plugin;

        connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() +
                "/data.db");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                    "id INTEGER PRIMARY KEY," +
                    "uuid TEXT NOT NULL," +
                    "x INTEGER NOT NULL," +
                    "y INTEGER NOT NULL," +
                    "z INTEGER NOT NULL," +
                    "world TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "level INTEGER NOT NULL," +
                    "exp INTEGER NOT NULL)", "cities"));
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(String.format("CREATE TABLE IF NOT EXISTS %s (" +
                    "id INTEGER NOT NULL," +
                    "uuid TEXT NOT NULL," +
                    "rank INTEGER NOT NULL)", "people"));
        }
    }

    @Override
    public void addCity(UUID owner_uuid, String name, Location location) {
        try (PreparedStatement stmt =
                     connection.prepareStatement("INSERT INTO cities (uuid, x, y, z, world, name, level, exp) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, owner_uuid.toString());
            stmt.setInt(2, (int) location.getX());
            stmt.setInt(3, (int) location.getY());
            stmt.setInt(4, (int) location.getZ());
            stmt.setString(5, location.getWorld().getName());
            stmt.setString(6, name);
            stmt.setInt(7, 1);
            stmt.setInt(8, 0);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    addPeople(owner_uuid, rs.getInt(1));
                    editRank(owner_uuid, 4);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }
    }

    @Override
    public void addPeople(UUID uuid, int CityID) {
        try (PreparedStatement stmt =
                     connection.prepareStatement("INSERT INTO people (id, uuid, rank) VALUES (?, ?, ?)")) {
            stmt.setInt(1, CityID);
            stmt.setString(2, uuid.toString());
            stmt.setInt(3, 1);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }
    }

    @Override
    public void editRank(UUID uuid, int rank) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE people SET rank = ? WHERE uuid = ?")) {
            stmt.setInt(1, rank);
            stmt.setString(2, uuid.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }
    }

    @Override
    public void editName(int CityID, String new_name) {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE cities SET name = ? WHERE id = ?")) {
            stmt.setString(1, new_name);
            stmt.setInt(2, CityID);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }
    }

    @Override
    public CityDetails getCityDetails(int CityID) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM cities WHERE id = ?")) {
            stmt.setInt(1, CityID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new CityDetails(rs.getInt("id"), rs.getString("uuid"), rs.getInt("x"), rs.getInt("y"),
                        rs.getInt("z"), rs.getString("world"), rs.getString("name"), rs.getInt("level"), rs.getInt(
                                "exp"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }

        return null;
    }

    @Override
    public PeopleDetails getPeopleDetails(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM people WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PeopleDetails(rs.getInt("id"), rs.getString("uuid"), rs.getInt("rank"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }

        return null;
    }

    @Override
    public List<CityDetails> getCityList() {
        List<CityDetails> cities = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM cities")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cities.add(new CityDetails(rs.getInt("id"), rs.getString("uuid"), rs.getInt("x"), rs.getInt("y"),
                        rs.getInt("z"), rs.getString("world"), rs.getString("name"), rs.getInt("level"), rs.getInt(
                        "exp")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }

        return cities;
    }

    @Override
    public List<PeopleDetails> getPeopleList(int CityID) {
        List<PeopleDetails> people = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM people WHERE id = ?")) {
            stmt.setInt(1, CityID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                people.add(new PeopleDetails(rs.getInt("id"), rs.getString("uuid"), rs.getInt("rank")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }

        return people;
    }

    @Override
    public void removeCity(int CityID) {
        for (PeopleDetails people: getPeopleList(CityID))
            removePeople(people.getUUID(), CityID);

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM cities WHERE id = ?")) {
            stmt.setInt(1, CityID);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }
    }

    @Override
    public void removePeople(UUID uuid, int CityID) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM people WHERE id = ? AND uuid = ?")) {
            stmt.setInt(1, CityID);
            stmt.setString(2, uuid.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe(e.toString());
        }
    }

    public synchronized void close() throws SQLException {
        if (connection == null) {
            return;
        }
        if (!connection.isClosed()) {
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            connection.close();
        }

        connection = null;
    }
}