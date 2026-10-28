package ru.mr_hares.vexillum.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.mr_hares.vexillum.Vexillum.*;

public class CityDetails {
    private int CityID;
    private UUID owner_uuid;
    private Location location;
    private String name;
    private List<PeopleDetails> people = new ArrayList<>();
    private int level;
    private int exp;

    public CityDetails(int CityID, String owner_uuid, int x, int y, int z, String world, String name, int level,
                       int exp) {
        this.CityID = CityID;
        this.owner_uuid = UUID.fromString(owner_uuid);
        this.location = new Location(Bukkit.getWorld(world), x, y, z);
        this.name = name;
        this.people = getDB().getPeopleList(CityID);
        this.level = level;
        this.exp = exp;
    }

    public int getCityID() { return CityID; }
    public UUID getOwnerUUID() { return owner_uuid; }
    public Location getLocation() { return location; }
    public String getName() { return name; }
    public List<PeopleDetails> getPeople() { return people; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
}
