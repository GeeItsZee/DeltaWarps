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

import com.gmail.tracebachi.DeltaWarps.Runnables.SyncGiveWarpsRunnable;
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.command.CommandSender;

import static com.gmail.tracebachi.DeltaWarps.Settings.failure;
import static com.gmail.tracebachi.DeltaWarps.Settings.input;


/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class GiveCommand implements IWarpCommand
{
    @Override
    public void shutdown() {}

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        String receiver = args[1];
        String warpTypeString = args[2];
        String amountString = args[3];

        if(!sender.hasPermission("DeltaWarps.Staff.Give"))
        {
            sender.sendMessage(Settings.noPermission("DeltaWarps.Staff.Give"));
            return;
        }

        WarpType type = WarpType.fromString(warpTypeString);

        if(type == WarpType.UNKNOWN)
        {
            sender.sendMessage(failure("Unknown warp type: " +
                input(warpTypeString)));
            return;
        }

        Integer amount = parseInt(amountString);

        if(amount == null || amount == 0)
        {
            sender.sendMessage(failure("Invalid number: " +
                input(amountString)));
            return;
        }

        SyncGiveWarpsRunnable runnable = new SyncGiveWarpsRunnable(
            sender,
            receiver,
            type,
            amount);

        runnable.run();
    }

    private Integer parseInt(String source)
    {
        try
        {
            return Integer.parseInt(source);
        }
        catch(NumberFormatException ex)
        {
            return null;
        }
    }
}
