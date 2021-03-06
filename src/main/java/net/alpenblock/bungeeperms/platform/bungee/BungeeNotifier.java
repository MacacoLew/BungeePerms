/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms.platform.bungee;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import net.alpenblock.bungeeperms.platform.proxy.NetworkType;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
public class BungeeNotifier implements NetworkNotifier
{

    private final ProxyConfig config;

    @Override
    public void deleteUser(User u, String origin)
    {
        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "deleteUser;" + u.getUUID(), origin);
        }
        else
        {
            sendPM(u.getName(), "deleteUser;" + u.getName(), origin);
        }
    }

    @Override
    public void deleteGroup(Group g, String origin)
    {
        sendPMAll("deleteGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUser(User u, String origin)
    {
        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "reloadUser;" + u.getUUID(), origin);
        }
        else
        {
            sendPM(u.getName(), "reloadUser;" + u.getName(), origin);
        }
    }

    @Override
    public void reloadGroup(Group g, String origin)
    {
        sendPMAll("reloadGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUsers(String origin)
    {
        sendPMAll("reloadUsers", origin);
    }

    @Override
    public void reloadGroups(String origin)
    {
        sendPMAll("reloadGroups", origin);
    }

    @Override
    public void reloadAll(String origin)
    {
        sendPMAll("reloadall", origin);
    }

    public void sendUUIDAndPlayer(String name, UUID uuid)
    {
        if (config.isUseUUIDs())
        {
            sendPM(uuid, "uuidcheck;" + name + ";" + uuid, null);
        }
    }

    //bukkit-bungeeperms reload information functions
    private void sendPM(String player, String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                && !Statics.listContains(config.getNetworkServers(), pp.getServer().getInfo().getName()))
            {
                return;
            }
            if (config.getNetworkType() == NetworkType.ServerDependendBlacklist
                && Statics.listContains(config.getNetworkServers(), pp.getServer().getInfo().getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && pp.getServer().getInfo().getName().equalsIgnoreCase(origin))
            {
                return;
            }

            //send message
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
            sendConfig(pp.getServer().getInfo());
        }
    }

    private void sendPM(UUID player, String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                && !Statics.listContains(config.getNetworkServers(), pp.getServer().getInfo().getName()))
            {
                return;
            }
            if (config.getNetworkType() == NetworkType.ServerDependendBlacklist
                && Statics.listContains(config.getNetworkServers(), pp.getServer().getInfo().getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && pp.getServer().getInfo().getName().equalsIgnoreCase(origin))
            {
                return;
            }

            //send message
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
            sendConfig(pp.getServer().getInfo());
        }
    }

    private void sendPMAll(String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        for (ServerInfo si : ProxyServer.getInstance().getConfig().getServers().values())
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                && !Statics.listContains(config.getNetworkServers(), si.getName()))
            {
                return;
            }
            if (config.getNetworkType() == NetworkType.ServerDependendBlacklist
                && Statics.listContains(config.getNetworkServers(), si.getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && si.getName().equalsIgnoreCase(origin))
            {
                continue;
            }

            //send message
            si.sendData(BungeePerms.CHANNEL, msg.getBytes());
            sendConfig(si);
        }
    }

    private long lastConfigUpdate = 0;

    private void sendConfig(ServerInfo info)
    {
        synchronized (this)
        {
            long now = System.currentTimeMillis();
            if (lastConfigUpdate + 5 * 60 * 1000 < now)
            {
                lastConfigUpdate = now;
                info.sendData(BungeePerms.CHANNEL, ("configcheck"
                                                    + ";" + info.getName()
                                                    + ";" + config.getBackendType()
                                                    + ";" + config.isUseUUIDs()
                                                    + ";" + config.getResolvingMode()
                                                    + ";" + config.isGroupPermission()
                                                    + ";" + config.isUseRegexPerms()).getBytes());
            }
        }
    }
}
