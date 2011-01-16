package com.nijikokun.bukkit.WarpGate;

import com.nijikokun.bukkit.General.General;
import com.nijikokun.bukkit.iConomy.iConomy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * WarpGate v1.5
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
    public static String codename = "Coanda Effect";
    public static String version = "1.5";

    /**
     * Listener for the plugin system.
     */
    public iListen l = new iListen(this);

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
    public static String directory = "WarpGate" + File.separator;
    public static boolean show = true, iConomy = false;
    public static int cost = 10;
    public static String currency = "";

    public WarpGate(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

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
	Settings = new iProperty(directory + "WarpGate.settings");

	iConomy = Settings.getBoolean("gate-use-iConomy", false);
	cost = Settings.getInt("gate-cost", 10);

	if(iConomy) {
	    setupCurrency();
	}
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

    private void setupCommands() {
	Plugin test = this.getServer().getPluginManager().getPlugin("General");

	if(test != null) {
	    General General = (General)test;
	    General.l.save_command("/mycommand", " - Some &etext&f explaining what it does!");
	}
    }

    public void setupCurrency() {
	Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");

	if(test != null) {
	    iConomy iC = (iConomy)test;
	    this.currency = iC.currency;
	} else {
	    log.info(Messaging.bracketize(name) + " iConomy is not loaded. Turning iConomy support off.");
	    iConomy = false;
	}
    }
}
