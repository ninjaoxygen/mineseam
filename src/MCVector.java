/**
 * 
 * MCVector - trivial helper class for 3-part world vectors
 * 
 * Copyright (C) 2011 Chris Poole chris@hackernet.co.uk
 * See README for BSD license.
 *
 * 2011.03.04 - Initial revision
 * 
 */

public class MCVector {
	int x;
	int y;
	int z;
	
	public MCVector() { }
	public MCVector(int ix, int iy, int iz) { x = ix; y = iy; z = iz; }
	public MCVector(MCVector v) { x = v.x; y = v.y; z = v.z; }
	
	public MCVector add(int ax, int ay, int az) {
		return new MCVector(x + ax, y + ay, z + az);
	}
	
	public MCVector add(MCVector v) {
		return new MCVector(x + v.x, y + v.y, z + v.z);
	}
}

