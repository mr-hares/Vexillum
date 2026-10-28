package ru.mr_hares.vexillum.database;

import org.bukkit.Location;
import ru.mr_hares.vexillum.objects.CityDetails;
import ru.mr_hares.vexillum.objects.PeopleDetails;

import java.util.List;
import java.util.UUID;

public interface DataVoid {
    void addCity(UUID owner_uuid, String name, Location location);
    void addPeople(UUID uuid, int CityID);

    void editRank(UUID uuid, int rank);
    void editName(int CityID, String new_name);

    CityDetails getCityDetails(int CityID);
    PeopleDetails getPeopleDetails(UUID uuid);

    List<CityDetails> getCityList();
    List<PeopleDetails> getPeopleList(int CityID);

    void removeCity(int CityID);
    void removePeople(UUID uuid, int CityID);
}