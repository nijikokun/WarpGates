package com.nijikokun.bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * WarpGate v1.0
 * Copyright (C) 2011  Nijikokun <nijikokun@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class WarpGate extends JavaPlugin {
    /*
     * Loggery Foggery
     */
    public static final Logger log = Logger.getLogger("Minecraft");
    
    /*
     * Central Data pertaining directly to the plugin name & versioning.
     */
    public static String name = "WarpGate";
    public static String codename = "Time Rift";
    public static String version = "1.1";

    /**
     * Listener for the plugin system.
     */
    private final iListen l = new iListen(this);

    /**
     * Controller for permissions and security.
     */
    public static iWatch Watch = new iWatch();

    /**
     * Controller for permissions and security.
     */
    public static Warp Warps = new Warp();

    /**
     * Things the controller needs to watch permissions for
     */
    private final String[] watching = { "manage-gates", "list-gates", };

    /**
     * Default settings for the permissions
     */
    private final String[] defaults = { "admins name,","admins name,", };

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public static Misc Misc = new Misc();

    /**
     * Internal Properties controllers
     */
    public static iProperty Settings, Permissions;

    /*
     * Variables
     */
    public static String directory = "WarpGate/";
    public static boolean show = true;

    public WarpGate(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, plugin, cLoader);

        registerEvents();
	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") loaded");
    }

    public void onDisable() {
	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") disabled");
    }

    public void onEnable() {
	setup();
	setupPermissions();
	setupWarps();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, l, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, l, Priority.Normal, this);
    }

    public void setup() {
	(new File(directory)).mkdir();

	try {
	    (new File(directory + "gates.db")).createNewFile();
	} catch (IOException ex) {
	    log.info("Could not create warp database file.");
	}

	// Settings / Permissions
	Permissions = new iProperty(directory + "WarpGate.permissions");
    }

    public void setupPermissions() {
	for(int x = 0; x < watching.length; x++) {
	    Watch.add(watching[x], Permissions.getString("can-" + watching[x], defaults[x]));
	}
    }

    public void setupWarps() {
	try {
	    BufferedReader in = new BufferedReader(new FileReader(directory + "gates.db"));
	    String str;

	    while ((str = in.readLine()) != null) {
		Warps.add(str);
		log.info("Loading Gate: " + str);
	    }

	    in.close();
	} catch (IOException e) { }
    }
}
