package ru.mr_hares.vexillum.objects;

import java.util.UUID;
import static ru.mr_hares.vexillum.Vexillum.*;

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

    public String getRankName() {
        String rank_key = switch (rank) {
            case 4 -> "owner";
            case 3 -> "deputy";
            case 2 -> "treasurer";
            default -> "citizen";
        };

        return getInstance().getConfig().getString("alliance.roles." + rank_key, "Житель");
    }
}
