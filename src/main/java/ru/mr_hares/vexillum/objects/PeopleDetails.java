package ru.mr_hares.vexillum.objects;

import java.util.UUID;

public class PeopleDetails {
    private int CityID;
    private UUID uuid;
    private int rank;

    public PeopleDetails(int CityID, String uuid, int rank) {
        this.CityID = CityID;
        this.uuid = UUID.fromString(uuid);
        this.rank = rank;
    }

    public int getCityID() { return CityID; }
    public UUID getUUID() { return uuid; }
    public int getRank() { return rank; }
}
