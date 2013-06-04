/*
 * HookManager.java
 * 
 * Statistics
 * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.wolvencraft.yasp;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;

import org.bukkit.plugin.PluginManager;

import com.wolvencraft.yasp.settings.Module;
import com.wolvencraft.yasp.util.ExceptionHandler;
import com.wolvencraft.yasp.util.Message;
import com.wolvencraft.yasp.util.PatchFetcher.PatchType;
import com.wolvencraft.yasp.util.hooks.*;

public class HookManager {
    
    private List<PluginHook> activeHooks;
    
    public HookManager() {
        activeHooks = new ArrayList<PluginHook>();
    }
    
    public void onEnable() {
        PluginManager plManager = Statistics.getInstance().getServer().getPluginManager();
        
        Message.log(
                "+-------- [ Hook Manager ] --------+",
                "|" + Message.centerString("Hook Manager starting up", 34) + "|",
                "|" + Message.centerString("", 34) + "|"
                );
        
        for(ApplicableHook hook : ApplicableHook.values()) {
            if (plManager.getPlugin(hook.getPluginName()) == null) {
                Message.log("|" + Message.centerString(hook.getPluginName() + " is not found", 34) + "|");
            } else if (!hook.getModule().isEnabled()) {
                Message.log("|" + Message.centerString(hook.getPluginName() + " is disabled", 34) + "|");
            } else {
                Message.log("|" + Message.centerString(hook.getPluginName() + " has been enabled", 34) + "|");
                try {
                    PluginHook hookObj = hook.getHook().newInstance();
                    hookObj.onEnable();
                    activeHooks.add(hookObj);
                } catch (Throwable t) {
                    ExceptionHandler.handle(t);
                    continue;
                }
            }
        }
        
        Message.log("+----------------------------------+");
    }
    
    public void onDisable() {
        Message.log(
                "+-------- [ Hook Manager ] --------+",
                "|" + Message.centerString("Hook Manager shutting down", 34) + "|",
                "|" + Message.centerString("", 34) + "|"
                );
        
        for(PluginHook hook : activeHooks) {
            hook.onDisable();
            Message.log("|" + Message.centerString(hook.getType().getPluginName() + " is shutting down", 34) + "|");
        }
        
        Message.log("+----------------------------------+");
    }
    
    @Getter(AccessLevel.PUBLIC)
    public enum ApplicableHook {
        
        ADMIN_CMD       (AdminCmdHook.class, Module.AdminCmd, PatchType.AdminCmd, "AdminCmd"),
        BAN_HAMMER      (BanHammerHook.class, Module.BanHammer, PatchType.BanHammer, "BanHammer"),
        COMMAND_BOOK    (CommandBookHook.class, Module.CommandBook, PatchType.CommandBook, "CommandBook"),
        FACTIONS        (FactionsHook.class, Module.Factions, PatchType.Factions, "Factions"),
        JOBS            (null, Module.Jobs, PatchType.Jobs, "Jobs"),
        MCBANS          (null, Module.McBans, PatchType.MCBans, "MCBans"),
        MCMMO           (null, Module.McMMO, PatchType.MCMMO, "McMMO"),
        MOB_ARENA       (MobArenaHook.class, Module.MobArena, PatchType.MobArena, "MobArena"),
        PVP_ARENA       (PvpArenaHook.class, Module.PvpArena, PatchType.PvpArena, "PVPArena"),
        VANISH          (VanishHook.class, Module.Vanish, PatchType.Vanish, "VanishNoPacket"),
        VAULT           (VaultHook.class, Module.Vault, PatchType.Vault, "Vault"),
        WORLD_GUARD     (WorldGuardHook.class, Module.WorldGuard, PatchType.WorldGuard, "WorldGuard")
        ;
        
        @Getter(AccessLevel.PRIVATE) private Class<? extends PluginHook> hook;
        private Module module;
        private PatchType patch;
        private String pluginName;
        
        ApplicableHook(Class<? extends PluginHook> hook, Module module, PatchType patch, String pluginName) {
            this.hook = hook;
            this.module = module;
            this.patch = patch;
            this.pluginName = pluginName;
        }
        
    }
    
}