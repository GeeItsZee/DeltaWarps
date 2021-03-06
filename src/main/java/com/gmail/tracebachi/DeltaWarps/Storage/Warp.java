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
package com.gmail.tracebachi.DeltaWarps.Storage;

import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class Warp
{
    private final static Pattern separator = Pattern.compile(",");

    private final String name;
    private final int x;
    private final int y;
    private final int z;
    private final float yaw;
    private final float pitch;
    private final String world;
    private final WarpType type;
    private final String faction;
    private final String server;

    public Warp(String name, Location location, WarpType type, String faction, String server)
    {
        this(name, location.getBlockX(), location.getBlockY(), location.getBlockZ(),
            location.getYaw(), location.getPitch(), location.getWorld().getName(),
            type, faction, server);
    }

    public Warp(String name, int x, int y, int z, float yaw, float pitch, String world,
        WarpType type, String faction, String server)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(server);

        this.name = name.toLowerCase();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = Math.round(yaw);
        this.pitch = Math.round(pitch);
        this.world = world;
        this.type = type;
        this.faction = faction;
        this.server = server;
    }

    public String getName()
    {
        return name;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public float getYaw()
    {
        return yaw;
    }

    public float getPitch()
    {
        return pitch;
    }

    public String getWorld()
    {
        return world;
    }

    public WarpType getType()
    {
        return type;
    }

    public String getFaction()
    {
        return faction;
    }

    public String getServer()
    {
        return server;
    }

    @Override
    public String toString()
    {
        return name + "," + x + "," + y + "," + z + "," + yaw + "," + pitch +
            "," + world + "," + type + "," + faction + "," + server;
    }

    public static Warp fromString(String source)
    {
        String[] fields = separator.split(source, 10);
        String name = fields[0];
        int x = Integer.parseInt(fields[1]);
        int y = Integer.parseInt(fields[2]);
        int z = Integer.parseInt(fields[3]);
        float yaw = Float.parseFloat(fields[4]);
        float pitch = Float.parseFloat(fields[5]);
        String world = fields[6];
        WarpType type = WarpType.fromString(fields[7]);
        String faction = fields[8];
        String server = fields[9];

        return new Warp(name, x, y, z, yaw, pitch, world, type, faction, server);
    }
}
