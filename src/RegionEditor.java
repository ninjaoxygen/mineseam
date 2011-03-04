/**
 * 
 * RegionEditor loads an entire Region of chunks, then calls a WorldProcessor for each chunk that is present.
 * The WorldProcessor is allowed to write to any block in the current *region*, so it can work across chunk
 * boundaries although not region boundaries.
 * 
 * Copyright (C) 2011 Chris Poole chris@hackernet.co.uk
 * See README for BSD license.
 * 
 * 2011.03.04 - Initial revision
 * 
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

interface WorldProcessor
{
	public void setRegionEditor(RegionEditor red);
	public void processArea(int x_min, int x_max, int y_min, int y_max, int z_min, int z_max);
}

public class RegionEditor {
	String regionString; // region file name
	RegionFile region;
	Tag chunk[][];
	byte[] blocks[][] = new byte[32][32][];
	byte[] height[][] = new byte[32][32][];

	public static final int regionsize = 32; // chunks in a region
	public static final int chunksize = 16; // blocks in a chunk
	public static final int chunkheight = 128; // height of a chunk

	public RegionEditor() {
	}

	public void load(File regionBaseFolder, String s) throws IOException {
		region = RegionFileCache.getRegionFile(regionBaseFolder, s);
		chunk = new Tag[regionsize][regionsize];
		regionString = s;

		for (int x = 0; x < regionsize; x++) {
			for (int z = 0; z < regionsize; z++) {
				if (region.hasChunk(x, z)) {
					DataInputStream chunkData = region.getChunkDataInputStream(x, z);
					Tag t = Tag.readFrom(chunkData);
					chunk[x][z] = t;
					Object o = t.findTagByName("Blocks").getValue();
					blocks[x][z] = (byte[])o;
					o = t.findTagByName("HeightMap").getValue();
					height[x][z] = (byte[])o;
				}
			}
		}
	}

	public void save(File regionBaseFolder, String s) throws IOException {
		RegionFile regionOut = RegionFileCache.getRegionFile(regionBaseFolder, s);

		for (int x = 0; x < regionsize; x++) {
			for (int z = 0; z < regionsize; z++) {
				if (chunk[x][z] != null) {
					// write out to a new area
					DataOutputStream chunkDataOut = regionOut.getChunkDataOutputStream(x, z);
					chunk[x][z].writeTo(chunkDataOut);
					chunkDataOut.close();
				}
			}
		}
	}

	public void apply(WorldProcessor p) {
		p.setRegionEditor(this);

		int cx = 0, cz = 0;
		for (cz = 0; cz < regionsize; cz++)
		{
			System.out.print("Processing chunk " + ( 100 * (cx + cz * regionsize) / (regionsize * regionsize)) + "%\r");
			for (cx = 0; cx < regionsize; cx++)
			{
				if (hasChunk(cx, cz))
				{
					p.processArea(cx * chunksize, (cx + 1) * chunksize - 1, 0, chunkheight - 1, cz * chunksize, (cz + 1) * chunksize - 1);
				}
			}
		}
		System.out.println("\nChunk complete");
	}
	
	public boolean hasChunk(int x, int z) {
		return (chunk[x][z] != null); 
	}

	public boolean inside(MCVector v) {
		return inside(v.x, v.y, v.z);
	}

	public boolean inside(int x, int y, int z) {
		if ((x < 0) || (z < 0) || (y < 0)) return false;
		if ((x >= chunksize * regionsize) || (z >= chunksize * regionsize)) return false;

		int chunkx = x / chunksize;
		int chunkz = z / chunksize;
		if (blocks[chunkx][chunkz] == null) return false;
		
		return true;
	}
	
	public byte getHeight(MCVector v) { // y component of v is ignored
		return getHeight(v.x, v.z);
	}

	public byte getHeight(int x, int z) {
		int chunkx = x / chunksize;
		int chunkz = z / chunksize;

		int blockx = x % chunksize;
		int blockz = z % chunksize;

		if ((x < 0) || (z < 0)) {
			throw new IllegalArgumentException("Out of bounds");
		}
		if ((x >= chunksize * regionsize) || (z >= chunksize * regionsize)) {
			throw new IllegalArgumentException("Out of bounds");
		}

		if (height[chunkx][chunkz] == null) {
			throw new IllegalArgumentException("Chunk not present");
		}

		return height[chunkx][chunkz][(blockx + chunksize * blockz)];
	}
	
	public byte get(MCVector v) {
		return get(v.x, v.y, v.z);
	}

	public byte get(int x, int y, int z) {
		int chunkx = x / chunksize;
		int chunkz = z / chunksize;

		int blockx = x % chunksize;
		int blockz = z % chunksize;

		if ((x < 0) || (z < 0)) {
			throw new IllegalArgumentException("Out of bounds");
		}
		if ((x >= chunksize * regionsize) || (z >= chunksize * regionsize)) {
			throw new IllegalArgumentException("Out of bounds");
		}

		if ((y < 0) || (y >= chunkheight)) {
			throw new IllegalArgumentException("Out of bounds (y)");
		}

		if (blocks[chunkx][chunkz] == null) {
			throw new IllegalArgumentException("Chunk not present");
		}

		return blocks[chunkx][chunkz][y + chunkheight * (blockz + chunksize * blockx)];
	}

	public int set(MCVector v, byte block) {
		return set(v.x, v.y, v.z, block);
	}
	
	public int set(int x, int y, int z, byte block) {
		int chunkx = x / chunksize;
		int chunkz = z / chunksize;

		int blockx = x % chunksize;
		int blockz = z % chunksize;

		if ((x < 0) || (z < 0)) {
			throw new IllegalArgumentException("Out of bounds");
		}
		if ((x >= chunksize * regionsize) || (z >= chunksize * regionsize)) {
			throw new IllegalArgumentException("Out of bounds");
		}

		if ((y < 0) || (y >= chunkheight)) {
			throw new IllegalArgumentException("Out of bounds (y)");
		}

		if (blocks[chunkx][chunkz] == null) {
			throw new IllegalArgumentException("Chunk not present");
		}

		blocks[chunkx][chunkz][y + chunkheight * (blockz + chunksize * blockx)] = block;

		return 0;
	}
}
