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
package com.gmail.tracebachi.DeltaWarps.Commands;

import com.gmail.tracebachi.DeltaExecutor.DeltaExecutor;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import com.gmail.tracebachi.DeltaWarps.Runnables.AddServerWarpRunnable;
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.gmail.tracebachi.DeltaWarps.Settings.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class SWarpCommand implements CommandExecutor, Registerable, Shutdownable
{
    private DeltaWarps plugin;

    public SWarpCommand(DeltaWarps plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void register()
    {
        plugin.getCommand("swarp").setExecutor(this);
    }

    @Override
    public void unregister()
    {
        plugin.getCommand("swarp").setExecutor(null);
    }

    @Override
    public void shutdown()
    {
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(failure("Only players can add warps."));
            return true;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("DeltaWarps.Staff.ServerWarp"))
        {
            player.sendMessage(Settings.noPermission("DeltaWarps.Staff.ServerWarp"));
            return true;
        }

        if(args.length == 0)
        {
            player.sendMessage(info("/swarp <warp name>"));
            return true;
        }

        String warpName = args[0].toLowerCase();

        if(IWarpCommand.reserved.contains(warpName))
        {
            sender.sendMessage(failure(input(warpName) + " is a reserved name."));
            return true;
        }

        if(warpName.length() > 31)
        {
            player.sendMessage(failure("Warp name size is restricted to 32 or less characters."));
            return true;
        }

        Warp warp = new Warp(
            warpName,
            player.getLocation(),
            WarpType.PUBLIC,
            null,
            DeltaRedisApi.instance().getServerName());
        AddServerWarpRunnable runnable = new AddServerWarpRunnable(
            sender.getName(),
            warp,
            plugin);

        DeltaExecutor.instance().execute(runnable);
        return true;
    }
}
