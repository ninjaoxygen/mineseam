/**
 * SeamBuilder does the work of scanning each region for suitable blocks and handling writing new blocks into the world.
 * the default behaviour is easy changed by creating other classes that implement WorldProcessor
 * 
 * Copyright (C) 2011 Chris Poole chris@hackernet.co.uk
 * See README for BSD license.
 * 
 * 2011.03.04 - Initial revision
 * 
 */

import java.security.InvalidParameterException;
import java.util.*;

public class SeamBuilder implements WorldProcessor {
	private int addMode = 0; // "brush" mode for adding blocks

	private final int seamMinCount = 10;
	private RegionEditor re;

	private TreeSet<Byte> replaceList;
	private TreeSet<Byte> oreList;
	
	private byte cleanWith; // when removing ore deposits, this is what we use
//	private byte replaceWith;

	public SeamBuilder() {
		replaceList = new TreeSet<Byte>();
		oreList = new TreeSet<Byte>();

		// configure block types that we are allowed to overwrite with new ore seam
		replaceList.add((byte) 1); // stone
//		replaceList.add((byte) 2); // grass
//		replaceList.add((byte) 3); // dirt
		replaceList.add((byte) 4); // cobblestone
//		replaceList.add((byte) 12); // sand
//		replaceList.add((byte) 13); // gravel
//		replaceList.add((byte) 16); // coal ore
//		replaceList.add((byte) 24); // sandstone
		
		// configure ores to re-seam
//		oreList.add((byte) 14); // gold ore
//		oreList.add((byte) 15); // iron ore
//		oreList.add((byte) 16); // coal ore
		oreList.add((byte) 56); // diamond ore
		
		cleanWith = 1; // return to stone when removing ore
	}
	
	public boolean replaceIf(MCVector v, TreeSet<Byte>list, byte with) {
		byte blockType;

		try {
			blockType = re.get(v);
			if (list.contains(blockType)) {
				re.set(v, with);
				return true;
			}
		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
		}
		return false;
	}

	// draw blocks into the world replacing only the given set 
	public void AddAt(MCVector v, byte blockType) {
		switch(addMode)
		{
		case 0: // basic, we add a single block
			replaceIf(v, replaceList, blockType);
			break;

		case 1: // the block plus each contacted face
			replaceIf(v, replaceList, blockType);
			replaceIf(v.add( 0,  0, -1), replaceList, blockType);
			replaceIf(v.add( 0,  0,  1), replaceList, blockType);
			replaceIf(v.add( 0, -1,  0), replaceList, blockType);
			replaceIf(v.add( 0,  1,  0), replaceList, blockType);
			replaceIf(v.add(-1,  0,  0), replaceList, blockType);
			replaceIf(v.add( 1,  0,  0), replaceList, blockType);
			break;
		}
	}

	// All the function in SeamBuilder rely on the use of a RegionEditor to provide access to the block data
	public void setRegionEditor(RegionEditor red) {
		re = red;
	}
	
	// clean a contiguous set of block back to the given type

	private int cleanContiguous(MCVector iv) {
		Stack<MCVector> s = new Stack<MCVector>();
		byte b; // block type to clean
		MCVector v = new MCVector(iv);
		int blocksRemoved = 0;

		b = re.get(v);
		s.push(v);
		
		if (cleanWith == b) {
			throw new RuntimeException("Trying to clean with one of the target types!");
		}

		while(!s.empty()) {
			v = s.pop();
			try {
				if (re.get(v) == b) {
					re.set(v, cleanWith);
					blocksRemoved++;

					// add all 26 surrounding blocks to the test list
					s.push(v.add( -1, -1, -1));
					s.push(v.add(  0, -1, -1));
					s.push(v.add(  1, -1, -1));
					s.push(v.add( -1,  0, -1));
					s.push(v.add(  0,  0, -1));
					s.push(v.add(  1,  0, -1));
					s.push(v.add( -1,  1, -1));
					s.push(v.add(  0,  1, -1));
					s.push(v.add(  1,  1, -1));
					s.push(v.add( -1, -1,  0));
					s.push(v.add(  0, -1,  0));
					s.push(v.add(  1, -1,  0));
					s.push(v.add( -1,  0,  0));
					s.push(v.add(  1,  0,  0));
					s.push(v.add( -1,  1,  0));
					s.push(v.add(  0,  1,  0));
					s.push(v.add(  1,  1,  0));
					s.push(v.add( -1, -1,  1));
					s.push(v.add(  0, -1,  1));
					s.push(v.add(  1, -1,  1));
					s.push(v.add( -1,  0,  1));
					s.push(v.add(  0,  0,  1));
					s.push(v.add(  1,  0,  1));
					s.push(v.add( -1,  1,  1));
					s.push(v.add(  0,  1,  1));
					s.push(v.add(  1,  1,  1));
				}
			} catch(InvalidParameterException e) {
			} catch (IllegalArgumentException e) {
			}
		}
		return blocksRemoved;
	}
	
	public void fillSeam(Stack<MCVector> seamPath, byte blockType) {
		MCVector v;
		
		while (!seamPath.empty()) {
			v = seamPath.pop();
			
			try {
				re.set(v, blockType);
			} catch(IllegalArgumentException e) {
			}
		}
	}
	
	public Stack<MCVector> buildSeamPath(MCVector startPos, int blockCount) {
		Stack<MCVector> seamPath = new Stack<MCVector>();
		int x = startPos.x, y = startPos.y, z = startPos.z; // current x / y / z
		MCVector curPos = new MCVector(startPos);
		MCVector origPos;
		MCVector prevPos;
		int dx = 0, dz = 0; // direction of seam

		float a,b,c,d;
		float p = 0;
		boolean finished = false;
		
		int ctr = 0; // number of blocks placed
		a = 2.0f * (float)Math.random() + 1.0f;
		b = 2.0f * (float)Math.random() + 1.0f;
		c = 2.0f * (float)Math.random() + 1.0f;
		d = 2.0f * (float)Math.random() + 1.0f;

		origPos = new MCVector(curPos);

		// choose a direction 
		switch((int)(Math.random() * 4))
		{
		case 0: dx =  1; dz =  0; break;
		case 1: dx = -1; dz =  0; break;
		case 2: dx =  0; dz =  1; break;
		case 3: dx =  0; dz = -1; break;
		default: throw(new RuntimeException("Direction random is bad!"));
		}

		while (!finished) {
			prevPos = new MCVector(curPos);
			p = p + 0.3f;
			curPos.x = (int) (origPos.x + p * dx + dz * (a * Math.sin(p / b) + c * Math.cos(p / d))); 
			curPos.z = (int) (origPos.z + p * dz + dx * (a * Math.sin(p / b) + c * Math.cos(p / d)));

			if (!curPos.equals(prevPos)) {
				ctr++;
				
				if ((!re.inside(x, y, z)) || (ctr >= blockCount)) {
					finished = true;
				} else {
					seamPath.push(new MCVector(curPos));
				}
			}
		}
		
		return seamPath;
	}

	public void processArea(int x_min, int x_max, int y_min, int y_max, int z_min, int z_max) {
		MCVector v = new MCVector();
		
		Stack<MCVector> s = new Stack<MCVector>();
		Stack<Byte> b = new Stack<Byte>();
		Stack<Integer> count = new Stack<Integer>();
		byte blockType;
		int blockCount;
		
		// Look through the entire region making a list of each piece of ore we find, then remove it
		// if we do not remove, we will get lots of ore in the same deposit
		for (v.y = y_min; v.y <= y_max; v.y++) {
			for (v.z = z_min; v.z <= z_max; v.z++) {
				for (v.x = x_min; v.x <= x_max; v.x++) {
					blockType = re.get(v);
					if (oreList.contains(blockType)) {
						s.push(new MCVector(v));
						b.push(new Byte(blockType));
						count.push(cleanContiguous(new MCVector(v)));
					}
				}
			}
		}
		
		// 
		while (!s.empty()) {
			v = s.pop();
			blockType = b.pop();
			blockCount = Math.max(seamMinCount, count.pop());
			
			fillSeam(buildSeamPath(v, blockCount), blockType);
		}
	}
}
