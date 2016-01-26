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

import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisMessageEvent;
import com.gmail.tracebachi.DeltaWarps.Runnables.FactionWarpsToPrivateRunnable;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/19/15.
 */
public class DeltaWarpsListener implements Listener
{
    private static final String WARP_CHANNEL = "DW-Warp";
    private static final Pattern PATTERN = Pattern.compile("/\\\\");

    private final String serverName;
    private HashMap<String, WarpRequest> warpRequests = new HashMap<>();
    private DeltaWarps plugin;

    public DeltaWarpsListener(String serverName, DeltaWarps plugin)
    {
        this.serverName = serverName;
        this.plugin = plugin;
    }

    public void shutdown()
    {
        plugin = null;
    }

    @EventHandler
    public void onRedisMessage(DeltaRedisMessageEvent event)
    {
        if(event.getChannel().equals(WARP_CHANNEL))
        {
            String[] splitMessage = PATTERN.split(event.getMessage(), 2);
            String name = splitMessage[0];
            Warp warp = Warp.fromString(splitMessage[1]);
            Player player = Bukkit.getPlayer(name);

            if(player != null && player.isOnline())
            {
                Location location = new Location(player.getWorld(),
                    warp.getX() + 0.5, warp.getY(), warp.getZ() + 0.5,
                    warp.getYaw(), warp.getPitch());
                warpPlayerWithEvent(player, location);
            }
            else
            {
                warpRequests.put(name, new WarpRequest(warp));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        WarpRequest request = warpRequests.remove(event.getPlayer().getName());

        if(request != null)
        {
            Location location = new Location(player.getWorld(),
                request.warp.getX() + 0.5, request.warp.getY(), request.warp.getZ() + 0.5,
                request.warp.getYaw(), request.warp.getPitch());
            warpPlayerWithEvent(player, location);
        }
    }

    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerLeaveFaction(EventFactionsMembershipChange event)
    {
        if(event.getReason() == EventFactionsMembershipChange.MembershipChangeReason.DISBAND ||
            event.getReason() == EventFactionsMembershipChange.MembershipChangeReason.KICK ||
            event.getReason() == EventFactionsMembershipChange.MembershipChangeReason.LEAVE)
        {
            MPlayer mPlayer = event.getMPlayer();

            FactionWarpsToPrivateRunnable runnable = new FactionWarpsToPrivateRunnable(
                mPlayer.getName(), serverName, plugin);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public void cleanup()
    {
        Iterator<Map.Entry<String, WarpRequest>> iterator = warpRequests.entrySet().iterator();
        long oldestTime = System.currentTimeMillis() - 5000;

        while(iterator.hasNext())
        {
            if(iterator.next().getValue().createdAt < oldestTime)
            {
                iterator.remove();
            }
        }
    }

    private class WarpRequest
    {
        private final Warp warp;
        private final long createdAt;

        public WarpRequest(Warp warp)
        {
            this.warp = warp;
            this.createdAt = System.currentTimeMillis();
        }
    }

    private void warpPlayerWithEvent(Player player, Location location)
    {
        PlayerWarpEvent event = new PlayerWarpEvent(player, location);
        Bukkit.getPluginManager().callEvent(event);

        if(!event.isCancelled())
        {
            player.teleport(location);
        }
    }
}
