package ru.mr_hares.vexillum;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mr_hares.vexillum.command.AllianceCommand;
import ru.mr_hares.vexillum.command.VexillumCommand;
import ru.mr_hares.vexillum.database.DataBase;
import ru.mr_hares.vexillum.loaders.MessagesLoader;
import ru.mr_hares.vexillum.utils.ColorUtil;

import java.sql.SQLException;

public final class Vexillum extends JavaPlugin {
    private static Vexillum instance;
    private static DataBase DB;
    private static MessagesLoader ML;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            DB = new DataBase(this);
            getLogger().info("Establishing a connection to the plugin database");
        } catch (SQLException e) {
            getLogger().severe("Error when opening the connection");
        }

        try {
            ML = new MessagesLoader(this);
        } catch (NullPointerException e) {
            getLogger().severe("Error loading the list of languages");
        }

        new AllianceCommand();
        new VexillumCommand();
    }

    @Override
    public void onDisable() {
        try {
             DB.close();
             getLogger().info("Closing the connection to the plugin database");
        } catch (SQLException e) {
             getLogger().severe("Error during connection closure");
        }
    }

    public static Vexillum getInstance() { return instance; }
    public static DataBase getDB() { return DB; }
    public static MessagesLoader getML() { return ML; }
}
