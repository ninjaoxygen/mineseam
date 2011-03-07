/**
 * 
 * MineSeam - a utility for Minecraft beta maps to
 * convert existing mineral deposits into seams.
 * 
 * Copyright (C) 2011 Chris Poole chris@hackernet.co.uk
 * See README for BSD license.
 * 
 * 2011.03.04 - Initial revision
 * 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class MineSeam {

	public static File getMinecraftDir() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String home = System.getProperty("user.home", ".");

		if (os.contains("win")) {
			String appdata = System.getenv("APPDATA");
			if (appdata != null) {
				return new File(appdata, ".minecraft");
			} else {
				return new File(home, ".minecraft");
			}
		} else if (os.contains("mac")) {
			return new File(home, "Library/Application Support/minecraft");
		} else {
			return new File(home, ".minecraft");
		}
	}

	public static void readLevel(String worldDir) throws Exception {
		try {
			Tag level = Tag.readFrom(new GZIPInputStream(new FileInputStream(worldDir + "level.dat")));
			int expectedVersion = 19132; // map constant for Beta 1.3 maps
			//level.print();
			int v = (Integer) level.findTagByName("version").getValue();
			System.out.println("Map version: " + v);
			
			if (v != expectedVersion) {
				throw new Exception("Map version is not " + expectedVersion);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("MineSeam v0.1");
			
			if (args.length != 1) {
				System.out.println("Usage: java -jar mineseam worldfile\r\n\n  where \"worldfile\" is a directory names within saves folder");
				return;
			}
			
			String worldToModify = args[0];
			
			String worldin = getMinecraftDir() + "/saves/" + worldToModify + "/"; 
			String worldout = getMinecraftDir() + "/saves/" + worldToModify + "/";
			
			if (Options.getOptions().doBackup) {
				// Backup the world before we do anything
				System.out.println("Backing up world " + worldToModify);
				ZipWorld.doZip(getMinecraftDir() + "/saves/", worldToModify);
			}

		    // Ensure the output path exists
		    //(new File(worldout + "region/")).mkdirs();
			
			System.out.println("Processing world...");
			
			File worldInFile = new File(worldin);
			File worldOutFile = new File(worldout);
			
			int x_min = 0, x_max = 0, z_min = 0, z_max = 0; // world limits by region file, assume a world based around 0,0

			// examine level.dat to check the map format
			try {
				readLevel(worldin);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			SeamBuilder sb = new SeamBuilder();
			RegionEditor re = new RegionEditor();
			MapBuilder mb = new MapBuilder();
			
			mb.setRegionEditor(re);

			File fo = new File(worldin + "region/");
			if(fo.isDirectory()) {
				String internalNames[] = fo.list();
				String fn;
				for(int i = 0; i < internalNames.length; i++) {
					File rf = new File(fo.getAbsolutePath() + "/" + internalNames[i]);
					if (rf.isFile()) {
						System.out.println(internalNames[i]);

						// Parse filename to get chunk address, looking for eg "r.-1.2.mcr"
						String bits[] = internalNames[i].split("\\.");
						int rx = Integer.parseInt(bits[1]);
						int rz = Integer.parseInt(bits[2]);
						
						x_min = Math.min(x_min, rx);
						x_max = Math.max(x_max, rx);
						z_min = Math.min(z_min, rz);
						z_max = Math.max(z_max, rz);
						
						re.load(worldInFile, internalNames[i]);
						
						// draw "before" map
						if (Options.getOptions().mapBefore) {
							mb.setMapPrefix("map_before_");
							mb.drawMap((byte)56); // diamond ore
						}
						
						// Apply changes
						re.apply(sb);
						
						// draw "after" map
						if (Options.getOptions().mapAfter) {
							mb.setMapPrefix("map_after_");
							mb.drawMap((byte)56); // diamond ore
						}
						
						re.save(worldOutFile, internalNames[i]);
					}
				}
			}
			// write HTML for "before" map
			if (Options.getOptions().mapBefore) {
				mb.setMapPrefix("map_before_");
				mb.writeHTML(x_min, x_max, z_min, z_max);
			}
			// write HTML for "after" map
			if (Options.getOptions().mapAfter) {
				mb.setMapPrefix("map_after_");
				mb.writeHTML(x_min, x_max, z_min, z_max);
			}
			System.out.println("Finished!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
