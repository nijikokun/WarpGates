package com.nijikokun.bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.bukkit.Location;

import org.bukkit.Player;

/**
 * WarpGate v1.0
 * Copyright (C) 2010  Nijikokun <nijikokun@gmail.com>
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

/**
 * Warp.java
 *
 * Permission handler
 *
 * @author Nijiko
 */
public class Warp {

    public static ArrayList<String> gates = new ArrayList<String>();

    public Warp() {

    }

    public static void save() {
	File file = new File(WarpGate.directory + "gates.db");

	try {
	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);

	    for (int i = 0; i < gates.size(); i++) {
		out.println((String) gates.get(i));
	    }

	    out.close();
	} catch (IOException e) {
	    WarpGate.log.info("Could not write to chunk.db!");
	}
    }

    public static void add(String name, int x, int y, int z, float rot, String link) {
	gates.add(name + "," + x + "," + y + "," + z + ","+ rot +"," + link + ",");
	save();
    }

    public static void add(String gate) {
	gates.add(gate);
    }

    public static void replace(int index, String name, int x, int y, int z, float rot, String link) {
	gates.remove(index);
	gates.add(name + "," + x + "," + y + "," + z + ","+ rot +"," + link + ",");
	save();
    }

    public static void get(int index) {
	gates.get(index);
	save();
    }

    public static void remove(int index) {
	gates.remove(index);
	save();
    }

    public static boolean exists(String name) {
	for(String gate : gates) {
	    if(getName(gate).equalsIgnoreCase(name)) {
		return true;
	    }
	}

	return false;
    }

    public static String getGate(String name) {
	for(String gate : gates) {
	    if(getName(gate).equalsIgnoreCase(name)) {
		return gate;
	    }
	}

	return null;
    }

    public static int getGateIndex(String name) {
	for(int i = 0; i < gates.size(); i++) {
	    if(getName(gates.get(i)).equalsIgnoreCase(name)) {
		return i;
	    }
	}

	return -1;
    }

    public static Location getExit(Player player, String gate) {
	Direction direction = getDirection(getDirectionalIndex(getRot(gate)));
	Location exit = new Location(player.getWorld(), getX(gate), getY(gate), getZ(gate));

        if (direction == Direction.EAST) {
            exit.setYaw(180.0F);
            exit.setZ(exit.getZ()-2.0D);
        } else if (direction == Direction.SOUTH) {
            exit.setYaw(270.0F);
            exit.setX(exit.getX()+2.0D);
        } else if (direction == Direction.WEST) {
            exit.setYaw(0.0F);
            exit.setZ(exit.getZ()+2.0D);
        } else if (direction == Direction.NORTH) {
            exit.setYaw(90.0F);
            exit.setX(exit.getX()-2.0D);
        }

	exit.setX(exit.getX()+0.5D);
	exit.setZ(exit.getZ()+0.5D);

	return exit;
    }

    public static boolean checkEntrance(int[] point, String gate) {
	String[] gate_data = parse(gate);
	Direction direction = getDirection(getDirectionalIndex(getRot(gate_data)));

	int x = getX(gate_data);
	int y = getY(gate_data);
	int z = getZ(gate_data);

	int px = point[0];
	int py = point[1];
	int pz = point[2];

	if (direction == Direction.EAST) {
	    //if((x == px && y == py && pz == z) || (x == px && y == py && pz == (z-1)) || (x == px && y == py && pz == (z-2))) { return true; }
	    //if(((x-1) == px && y == py && pz == (z-1)) || ((x-1) == px && y == py && pz == (z-2))) { return true; }
	    if((x == px && y == py && pz == z) || (x == px && y == py && pz == (z-1))) { return true; }
	    if(((x-1) == px && y == py && pz == (z-1))) { return true; }
	} else if (direction == Direction.SOUTH) {
	    if((x == px && y == py && pz == z) || ((x+1) == px && y == py && pz == z)) { return true; }
	    if(((x+1) == px && y == py && pz == (z-1))) { return true; }
	    //if(((x+2) == px && y == py && pz == (z-2)) || ((x+2) == px && y == py && pz == (z-2))) { return true; }
	} else if (direction == Direction.WEST) {
	    //if((x == px && y == py && pz == z) || (x == px && y == py && pz == (z+1)) || (x == px && y == py && pz == (z+2))) { return true; }
	    //if(((x-1) == px && y == py && pz == (z+1)) || ((x-1) == px && y == py && pz == (z+2))) { return true; }
	    if((x == px && y == py && pz == z) || (x == px && y == py && pz == (z+1))) { return true; }
	    if(((x-1) == px && y == py && pz == (z+1))) { return true; }
	} else if (direction == Direction.NORTH) {
	    if((x == px && y == py && pz == z) || ((x-1) == px && y == py && pz == z)) { return true; }
	    if(((x-1) == px && y == py && pz == (z-1))) { return true; }
	    //if(((x-1) == px && y == py && pz == (z+2)) || ((x-2) == px && y == py && pz == (z+2))) { return true; }
	}

	return false;
    }

    public static String getDestination(String gate, String link, int tries) {
	if(link == null || link.equals("null")) { return null; }
	if(tries > 6) { return link; }

	String[] linkdata = Warp.parse(Warp.getGate(link));
	String linked = (linkdata.length < 6) ? null : linkdata[5];

	if(linked == null || linked.equals("null")) {
	    return link;
	}

	if(link.equalsIgnoreCase(linked) || !gate.equalsIgnoreCase(linked)) {
	    return linked;
	}

	tries++;
	return getDestination(linkdata[0], linked, tries);

    }

    public static int getDirectionalIndex(float rot) {
        double degrees = ((rot - 90) % 360);

        if (degrees < 0) {
            degrees += 360.0;
        }

        if (degrees >= 315 && degrees <= 45) {
            return 1; // N
        } else if (degrees >= 45 && degrees <= 135) {
            return 2; // E
        } else if (degrees >= 135 && degrees <= 225) {
            return 3; // S
        } else if (degrees >= 225 && degrees <= 315) {
            return 4; // W
        } else {
            return 1;
        }
    }

    public static Direction getDirection(int direction) {
	if(direction == 1) {
	    return Direction.NORTH;
	} else if(direction == 2) {
	    return Direction.EAST;
	} else if(direction == 3) {
	    return Direction.SOUTH;
	} else {
	    return Direction.WEST;
	}
    }

    public static String[] parse(String gate) {
	return gate.split(",");
    }

    public static String getName(String gate) {
	return parse(gate)[0];
    }

    public static int getX(String gate) {
	return Integer.valueOf(parse(gate)[1]);
    }

    public static int getY(String gate) {
	return Integer.valueOf(parse(gate)[2]);
    }

    public static int getZ(String gate) {
	return Integer.valueOf(parse(gate)[3]);
    }

    public static float getRot(String gate) {
	return Float.valueOf(parse(gate)[4]);
    }

    public static String getLink(String gate) {
	if(parse(gate).length < 6) {
	    return null;
	}

	return parse(gate)[5];
    }

    public static String getName(String[] gate) {
	return gate[0];
    }

    public static int getX(String[] gate) {
	return Integer.valueOf(gate[1]);
    }

    public static int getY(String[] gate) {
	return Integer.valueOf(gate[2]);
    }

    public static int getZ(String[] gate) {
	return Integer.valueOf(gate[3]);
    }

    public static float getRot(String[] gate) {
	return Float.valueOf(gate[4]);
    }

    public static String getLink(String[] gate) {
	if(gate.length < 6) {
	    return null;
	}

	return gate[5];
    }

    public static enum Direction {
	NORTH,
	EAST,
	SOUTH,
	WEST;
    }
}
