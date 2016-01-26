/*
 * This file is part of DeltaWarps.
 *
 * DeltaWarps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaWarps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaWarps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaWarps;

import com.gmail.tracebachi.DbShare.DbShare;
import com.gmail.tracebachi.DeltaEssentials.DeltaEssentials;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedis;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.gmail.tracebachi.DeltaWarps.Commands.WarpCommand;
import com.gmail.tracebachi.DeltaWarps.Runnables.UseWarpRunnable;
import com.gmail.tracebachi.DeltaWarps.Commands.SWarpCommand;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class DeltaWarps extends JavaPlugin
{
    private static final String CREATE_PLAYER_TABLE =
        " CREATE TABLE IF NOT EXISTS deltawarps_players (" +
        " `id`       INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
        " `name`     VARCHAR(32) NOT NULL UNIQUE KEY," +
        " `normal`   SMALLINT UNSIGNED NOT NULL DEFAULT 0," +
        " `faction`  SMALLINT UNSIGNED NOT NULL DEFAULT 0" +
        " );";
    private static final String CREATE_WARP_TABLE =
        " CREATE TABLE IF NOT EXISTS deltawarps_warps (" +
        " `name`      VARCHAR(32) NOT NULL PRIMARY KEY," +
        " `owner_id`  INT UNSIGNED NOT NULL," +
        " `x`         INT SIGNED NOT NULL," +
        " `y`         INT SIGNED NOT NULL," +
        " `z`         INT SIGNED NOT NULL," +
        " `yaw`       FLOAT NOT NULL," +
        " `pitch`     FLOAT NOT NULL," +
        " `type`      VARCHAR(7) NOT NULL," +
        " `faction`   VARCHAR(36)," +
        " `server`    VARCHAR(32) NOT NULL," +
        " KEY `faction_and_server` (`faction`, `server`)" +
        " );";
    private static final String SELECT_SERVER_WARP_OWNER =
        " SELECT 1 FROM deltawarps_players WHERE name = '!DeltaWarps!';";
    private static final String INSERT_SERVER_WARP_OWNER =
        " INSERT INTO deltawarps_players" +
        " (id, name, normal, faction)" +
        " VALUES(1, '!DeltaWarps!', 65535, 65535);";

    private String databaseName;
    private DeltaWarpsListener warpsListener;
    private DeltaRedisApi deltaRedisApi;
    private DeltaEssentials deltaEssentialsPlugin;
    private WarpCommand warpCommand;
    private SWarpCommand sWarpCommand;

    @Override
    public void onLoad()
    {
        saveDefaultConfig();
    }

    @Override
    public void onEnable()
    {
        DeltaRedis deltaRedis = (DeltaRedis) getServer().getPluginManager().getPlugin("DeltaRedis");
        deltaEssentialsPlugin = (DeltaEssentials) getServer().getPluginManager().getPlugin("DeltaEssentials");
        deltaRedisApi = deltaRedis.getDeltaRedisApi();
        databaseName = getConfig().getString("Database");

        if(checkDatabase() && createTables())
        {
            warpCommand = new WarpCommand(deltaRedisApi.getServerName(), this);
            getCommand("warp").setExecutor(warpCommand);
            sWarpCommand = new SWarpCommand(deltaRedisApi.getServerName(), this);
            getCommand("swarp").setExecutor(sWarpCommand);

            warpsListener = new DeltaWarpsListener(deltaRedisApi.getServerName(), this);
            getServer().getPluginManager().registerEvents(warpsListener, this);

            getServer().getScheduler().runTaskTimer(this, () -> warpsListener.cleanup(), 40, 40);
        }
        else
        {
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);

        if(warpsListener != null)
        {
            warpsListener.shutdown();
            warpsListener = null;
        }

        if(sWarpCommand != null)
        {
            getCommand("swarp").setExecutor(null);
            sWarpCommand.shutdown();
            sWarpCommand = null;
        }

        if(warpCommand != null)
        {
            getCommand("warp").setExecutor(null);
            warpCommand.shutdown();
            warpCommand = null;
        }
    }

    public Connection getDatabaseConnection() throws SQLException
    {
        return DbShare.getDataSource(databaseName).getConnection();
    }

    public void useWarpSync(String sender, String warper, String warpOwner, Warp warp)
    {
        Bukkit.getScheduler().runTask(this, new UseWarpRunnable(
            deltaRedisApi, deltaEssentialsPlugin, sender, warper, warpOwner, warp));
    }

    private boolean checkDatabase()
    {
        if(DbShare.getDataSource(databaseName) == null)
        {
            getLogger().severe("The specified database does not exist. Shutting down ...");
            return false;
        }
        return true;
    }

    private boolean createTables()
    {
        try(Connection connection = getDatabaseConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(CREATE_PLAYER_TABLE))
            {
                getLogger().info("Creating player table ...");
                statement.execute();
            }
            try(Statement statement = connection.createStatement())
            {
                try(ResultSet resultSet = statement.executeQuery(SELECT_SERVER_WARP_OWNER))
                {
                    if(!resultSet.next())
                    {
                        statement.execute(INSERT_SERVER_WARP_OWNER);
                    }
                }
            }
            try(PreparedStatement statement = connection.prepareStatement(CREATE_WARP_TABLE))
            {
                getLogger().info("Creating warps table ...");
                statement.execute();
            }
            return true;
        }
        catch(SQLException ex)
        {
            getLogger().severe("Failed to build required tables.");
            ex.printStackTrace();
            return false;
        }
    }
}