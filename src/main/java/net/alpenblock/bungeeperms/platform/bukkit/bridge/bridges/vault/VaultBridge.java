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
package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.vault;

import java.lang.reflect.Method;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.Bridge;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

public class VaultBridge implements Bridge
{

    @Override
    public void enable()
    {
        Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (plugin != null)
        {
            inject(plugin);
        }
    }

    @Override
    public void disable()
    {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (plugin != null)
        {
            uninject(plugin);
        }

        PluginEnableEvent.getHandlerList().unregister(this);
        PluginDisableEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("vault"))
        {
            return;
        }
        inject(e.getPlugin());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e)
    {
        if (!e.getPlugin().getName().equalsIgnoreCase("vault"))
        {
            return;
        }
        uninject(e.getPlugin());
    }

    public void inject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Injection of Bungeeperms into Vault");
        try
        {
            Vault v = (Vault) plugin;

            if (!v.isEnabled())
            {
                return;
            }

            //inject BungeePerms permissions
//            Method m = v.getClass().getDeclaredMethod("hookPermission", String.class, Class.class, ServicePriority.class, String[].class);
//            m.setAccessible(true);
//            m.invoke(v, "BungeePerms", Permission_BungeePerms.class, ServicePriority.Normal, new String[]
//             {
//                 "net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin"
//            });
//            
//            Field f = v.getClass().getDeclaredField("perms");
//            f.setAccessible(true);
//            f.set(v, Bukkit.getServicesManager().getRegistration(Permission.class).getProvider());
            
            Bukkit.getServer().getServicesManager().register(Permission.class, new Permission_BungeePerms(v), BukkitPlugin.getInstance(), ServicePriority.Highest);

            //inject BungeePerms chat
//            m = v.getClass().getDeclaredMethod("hookChat", String.class, Class.class, ServicePriority.class, String[].class);
//            m.setAccessible(true);
//            m.invoke(v, "BungeePerms", Chat_BungeePerms.class, ServicePriority.Normal, new String[]
//             {
//                 "net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin"
//            });
            
            Bukkit.getServer().getServicesManager().register(Chat.class, new Chat_BungeePerms(v, new Permission_BungeePerms(v)), BukkitPlugin.getInstance(), ServicePriority.Highest);
        }
        catch (Exception ex)
        {
            BungeePerms.getInstance().getDebug().log(ex);
        }
    }

    public void uninject(Plugin plugin)
    {
        BungeePerms.getLogger().info("Uninjection of Bungeeperms into Vault");

        try
        {
            Vault v = (Vault) plugin;

            if (!v.isEnabled())
            {
                return;
            }

            //uninject BungeePerms permissions
            Method m = v.getClass().getDeclaredMethod("loadChat");
            m.setAccessible(true);
            m.invoke(v);

            //inject BungeePerms chat
            m = v.getClass().getDeclaredMethod("loadPermission");
            m.setAccessible(true);
            m.invoke(v);
        }
        catch (Exception ex)
        {
            BungeePerms.getInstance().getDebug().log(ex);
        }
    }
}
