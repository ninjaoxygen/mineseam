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
			
			String worldin = getMinecraftDir() + "/saves/" + args[0] + "/"; 
			String worldout = getMinecraftDir() + "/saves/" + args[0] + "/";

		    // Ensure the output path exists
		    //(new File(worldout + "region/")).mkdirs();
			
			File worldInFile = new File(worldin);
			File worldOutFile = new File(worldout);

			// examine level.dat to check the map format
			try {
				readLevel(worldin);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			SeamBuilder sb = new SeamBuilder();
			RegionEditor re = new RegionEditor();
//			MapBuilder mb = new MapBuilder();
			
//			mb.setRegionEditor(re);

			File fo = new File(worldin + "region/");
			if(fo.isDirectory()) {
				String internalNames[] = fo.list();
				for(int i = 0; i < internalNames.length; i++) {
					File rf = new File(fo.getAbsolutePath() + "/" + internalNames[i]);
					if (rf.isFile()) {
						System.out.println(internalNames[i]);
						re.load(worldInFile, internalNames[i]);
						re.apply(sb);
//						mb.drawMap((byte)56); // diamond ore
						re.save(worldOutFile, internalNames[i]);
					}
				}
			}
//			mb.writeHTML();
			System.out.println("Finished!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
