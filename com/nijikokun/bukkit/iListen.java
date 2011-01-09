package com.nijikokun.bukkit;


import java.util.logging.Logger;
import org.bukkit.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import net.minecraft.server.WorldServer;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * iListen.java
 * <br /><br />
 * Listens for calls from bukkit, and reacts accordingly.
 * 
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class iListen extends PlayerListener {

    private static final Logger log = Logger.getLogger("Minecraft");

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public Misc Misc = new Misc();

    public static WarpGate plugin;
    public WarpGate p;
    public WorldServer server;

    public iListen(WarpGate instance) {
        plugin = instance;
        p = instance;
    }

    /**
     * Sends simple condensed help lines to the current player
     */
    private void showSimpleHelp() {
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f ChunkControl (&c"+WarpGate.codename+"&f)            ");
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f [] Required, () Optional                            ");
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f /warpgate|wg &6- &eShows this information.          ");
	Messaging.send("&f /wg create [Name] &6- &eCreates portal with no link ");
	Messaging.send("&f /wg link [Name] [To] &6- &eLink two portals together");
	Messaging.send("&f /wg list &6- &eList of portals for identifying them ");
	Messaging.send("&f /wg remove [Name] &6- &eRemoves portal, blocks stay.");
	Messaging.send("&e-----------------------------------------------------");
    }

    private boolean checkRotation(Player player) {
        double degrees = ((player.getLocation().getYaw() - 90) % 360);

        if (degrees < 0) {
            degrees += 360.0;
        }

        if (degrees >= 315 && degrees <= 45) {
            return true; // N
        } else if (degrees >= 45 && degrees <= 135) {
            return false; // E
        } else if (degrees >= 135 && degrees <= 225) {
            return true; // S
        } else if (degrees >= 225 && degrees <= 315) {
            return false; // W
        } else {
            return false;
        }
    }

    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
        String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
	Messaging.save(player);
	String base = split[0];

	if(Misc.isEither(base, "/warpgate", "/wg")) {
	    if(split.length < 2) {
		showSimpleHelp(); event.setCancelled(true);
		return;
	    }

	    String command = split[1];

	    if (Misc.isEither(command, "create", "-c") && split.length >= 2) {
		if (!WarpGate.Watch.permission("manage-gates", player)) {
		    return;
		}

		String name = split[2];

		if(name == null || name.isEmpty() || name.equals("")) {
		    Messaging.send("&cWarpGate name is invalid."); event.setCancelled(true);
		    return;
		}

		if(Warp.exists(name)) {
		    Messaging.send("&cWarpGate already exists by that name."); event.setCancelled(true);
		    return;
		}

		int x = player.getLocation().getBlockX();
		int y = player.getLocation().getBlockY();
		int z = player.getLocation().getBlockZ();
		float rot = player.getLocation().getYaw();
		boolean ux = checkRotation(player);

		for(int i = 0; i < 7; i++) {
		    if(i > 5) {
			if(i == 6) {
			    player.getWorld().getBlockAt(((!ux)?x+1:x),y+3,((ux)?z+1:z)).setTypeID(49);
			    player.getWorld().getBlockAt(x,y+3,z).setTypeID(49);
			    player.getWorld().getBlockAt(((!ux)?x-1:x),y+3,((ux)?z-1:z)).setTypeID(49);
			    player.getWorld().getBlockAt(((!ux)?x-2:x),y+3,((ux)?z-2:z)).setTypeID(49);
			}

			player.getWorld().getBlockAt(((!ux)?x-(i-4):x),y+3,((ux)?z-(i-4):z)).setTypeID(49);
		    }

		    if(i < 2) {
			player.getWorld().getBlockAt(((!ux)?x+i:x),y-1,((ux)?z+i:z)).setTypeID(49);
		    }

		    if(i < 3) {
			player.getWorld().getBlockAt(((!ux)?x-i:x),y-1,((ux)?z-i:z)).setTypeID(49);

			if(i == 2) {
			    player.getWorld().getBlockAt(((!ux)?x-i:x),y,((ux)?z-i:z)).setTypeID(49);
			    player.getWorld().getBlockAt(((!ux)?x+1:x),y,((ux)?z+1:z)).setTypeID(49);
			}
		    } else {
			player.getWorld().getBlockAt(((!ux)?x-2:x),y+(i-4),((ux)?z-2:z)).setTypeID(49);

			if(i < 6) {
			    player.getWorld().getBlockAt(((!ux)?x-1:x),y+(i-3),((ux)?z-1:z)).setTypeID(0);
			    player.getWorld().getBlockAt(x,y+(i-3),z).setTypeID(0);
			}

			player.getWorld().getBlockAt(((!ux)?x+1:x),y+(i-4),((ux)?z+1:z)).setTypeID(49);
		    }
		}

		player.getWorld().getBlockAt(((!ux)?x-1:x),y,((ux)?z-1:z)).setTypeID(51);

		Warp.add(name, x, y, z, rot, null);

		Messaging.send("&eCreated WarpGate: &f"+name+"."); event.setCancelled(true);
		return;
	    }
	    
	    if (Misc.isEither(command, "link", "-l") && split.length >= 3) {
		if (!WarpGate.Watch.permission("manage-gates", player)) {
		    return;
		}

		String name = split[2];
		String with = split[3];

		if(name == null || name.isEmpty() || name.equals("") || with == null || with.isEmpty() || with.equals("")) {
		    Messaging.send("&cWarpGate name is invalid."); event.setCancelled(true);
		    return;
		}

		if(!Warp.exists(name) || !Warp.exists(with)) {
		    Messaging.send("&cWarpGate does not exist by that name."); event.setCancelled(true);
		    return;
		}

		String gate = Warp.getGate(name);
		int x = Warp.getX(gate);
		int y = Warp.getY(gate);
		int z = Warp.getZ(gate);
		float rot = Warp.getRot(gate);
		int index = Warp.getGateIndex(name);

		WarpGate.Warps.replace(index, name, x, y, z, rot, with);

		Messaging.send("&eLinked WarpGate: &f"+name+" &ewith &f"+with+"."); event.setCancelled(true);
		return;
	    }

	    if (Misc.isEither(command, "unlink", "-ul") && split.length >= 2) {
		if (!WarpGate.Watch.permission("manage-gates", player)) {
		    return;
		}

		String name = split[2];

		if(name == null || name.isEmpty() || name.equals("")) {
		    Messaging.send("&cWarpGate name is invalid."); event.setCancelled(true);
		    return;
		}

		if(!Warp.exists(name)) {
		    Messaging.send("&cWarpGate does not exist by that name."); event.setCancelled(true);
		    return;
		}

		String gate = Warp.getGate(name);
		int x = Warp.getX(gate);
		int y = Warp.getY(gate);
		int z = Warp.getZ(gate);
		float rot = Warp.getRot(gate);
		int index = Warp.getGateIndex(name);

		Warp.replace(index, name, x, y, z, rot, null);

		Messaging.send("&eUn-Linked WarpGate: &f"+name+"&e."); event.setCancelled(true);
		return;
	    }

	    if (Misc.isEither(command, "remove", "-r") && split.length >= 2) {
		if (!WarpGate.Watch.permission("manage-gates", player)) {
		    return;
		}

		String name = split[2];
		String with = null;

		if(name == null || name.isEmpty() || name.equals("")) {
		    Messaging.send("&cWarpGate name is invalid."); event.setCancelled(true);
		    return;
		}

		if(!Warp.exists(name)) {
		    Messaging.send("&cWarpGate does not exist by that name."); event.setCancelled(true);
		    return;
		}

		if(Warp.parse(Warp.getGate(name)).length > 5) {
		    with = Warp.getLink(name);
		}

		Messaging.send("&eRemoved WarpGate: &f"+name+"!");

		if(with != null || !with.equals("null")) {
		    Messaging.send("&eLinked Broken Between: &f"+name+" &eand &f"+with+".");
		}

		Warp.remove(Warp.getGateIndex(name));
		event.setCancelled(true); return;
	    }

	    if (Misc.isEither(command, "list", "-x")) {
		if (!WarpGate.Watch.permission("list-gates", player)) {
		    return;
		}

		for (int i = 0; i < Warp.gates.size(); i++) {
		    String[] gate = Warp.parse(Warp.gates.get(i));
		    String name = gate[0];
		    String x = gate[1];
		    String y = gate[2];
		    String z = gate[3];
		    String rot = gate[4];
		    String link = gate[5];
		    
		    String to = "";

		    if(link != null || !link.equals("null")) {
			to = " &eto&f "+link;
		    }

		    Messaging.send("WarpGate #&e[&f"+i+"&e]&f "+name+to+" &e[&f"+x+"&ex&f "+y+"&ey&f "+z+"&ez]");
		}

		event.setCancelled(true);
		return;
	    }


	}
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
	Player player = event.getPlayer();
	
	int[] points = { event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ() };

	for(String gate : Warp.gates) {
	    String[] data = Warp.parse(gate);
	    if(data.length < 6) { continue; }
	    String name = data[0];
	    String link = data[5];
	    String linked = "";

	    if(link == null || link.equals("null") || !Warp.exists(link)) {
		continue;
	    }

	    linked = Warp.getGate(Warp.getDestination(name, link, 1));

	    if(linked == null || link.equals("null")) {
		continue;
	    }

	    if(Warp.checkEntrance(points, gate)) {
		Location destination = Warp.getExit(player, linked);

		player.teleportTo(destination);
		event.setTo(destination);
		break;
	    } else {
		continue;
	    }
	}
    }
}
