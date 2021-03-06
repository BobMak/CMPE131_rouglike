import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import PCG.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import PCG.Tunneller;

class MapValidity {

	MapGenerator mapGen;
	
	@BeforeEach
	void init() {
		mapGen = new MapGenerator();
	}
	
	//Pick the first iterated block, then recursively finds adjacent block to put into the theOnly hashset.
	//If there's any other block (found using a 2nd iteration through map) 
	//that is a '1' block and not in theOnly, 
	//map is disconnected hence invalid
	boolean testMapConnectivity(int[][] testMap) {
		HashSet<Coordinates> theOnly = new HashSet<Coordinates>(testMap.length*testMap[0].length);
		Stack<Coordinates> toDo    = new Stack<Coordinates>();
		int initialX = -1;
		int initialY = -1;
		outerloop:
		for (int i=0; i<testMap.length; i++) {
			for (int j=0; j<testMap[0].length; j++) {
				if (testMap[i][j] == 1) {
					initialX = j;
					initialY = i;
					break outerloop;
				}
			}
		}
		recForOnly (initialX, initialY, testMap, theOnly, toDo, 0);
		Coordinates c;
		while ( !toDo.isEmpty() ) {
			c = toDo.pop();
			initialX = c.getX();
			initialY = c.getY();
			recForOnly (initialX, initialY, testMap, theOnly, toDo, 0);
		}
		boolean check = true;
		outerloop2:
		for (int i=0; i<testMap.length; i++) {
			for (int j=0; j<testMap[0].length; j++) {
				if (testMap[i][j]==1) //'1' block detected
				{
					Coordinates coor = new Coordinates(j,i);
					if (!theOnly.contains(coor))	//theOnly must contain this '1' block
					{
						check = false;
						break outerloop2;
					}
					else check = true;
				}
				else check = true;
			}
		} 
		return check;
	}
	
	//Recursively loads values for theOnly hashSet
	void recForOnly(int curX, int curY, int[][] testMap,
									HashSet<Coordinates> theOnly,
									Stack<Coordinates> toDo, int rec) {
		Coordinates coor = new Coordinates(curX,curY);
		if(!theOnly.add(coor))
			return;
		for (int i=curY-1; i<curY+2; i++) 
			for (int j=curX-1; j<curX+2; j++) 
				if (i>=0 && i<testMap.length   &&   j>=0 && j<testMap[0].length) 
					if (testMap[i][j]!=0 && (curX!=j || curY!=i)) {
						if ( rec < 6 ) {
//							theOnly.add(new Coordinates(j,i));
							recForOnly(j, i, testMap, theOnly, toDo, rec++);
						}
						else {
							toDo.add(new Coordinates(j,i));
						}
					}
	}
	
	@Test
	void testVailidy1 () {
		int[][] test = {{1,1,1},
			{1,0,0},
			{0,0,0}};
		if (testMapConnectivity(test)==false) fail("Nope");
	}

	@Test
	void testVailidy2 () {
		int[][] test = {{1,1,1},
			{1,1,0},
			{0,0,0}};
		if (testMapConnectivity(test)==false) fail("Nope");
	}

	@Test
	void testTunneller () {
		mapGen.setConfigAlgorithm("Tunneller");
		String[][] defaults = mapGen.getDefaultConfig();
		String[][] vectors = new String[ 1000 ][ defaults.length ];
		for ( int idx=0; idx < vectors.length; idx++ ) {
			vectors[idx][0]  = ""+Util.randint(0, 40);  															  // room dimension max
			vectors[idx][1]  = ""+Util.randint(0, Integer.parseInt( vectors[idx][0]) );	// room dimension min
			vectors[idx][2]  = ""+Util.randint(0, 500);																	// primary corridor length max
			vectors[idx][3]  = ""+Util.randint(0, Integer.parseInt( vectors[idx][2] )); // primary corridor length min
			vectors[idx][4]  = ""+Util.randint(1, 6);																		// primary corridor width
			vectors[idx][5]  = ""+Util.randint(1, Integer.parseInt( vectors[idx][3] )); // primary corridor distance between turns max
			vectors[idx][6]  = ""+Util.randint(1, Integer.parseInt( vectors[idx][5] )); // primary corridor distance between turns min
			vectors[idx][7]  = ""+(float) Math.random();																// primary corridor branching probability
			vectors[idx][8]  = ""+Util.randint(1, 20);																	// primary corridors
			vectors[idx][9]  = ""+Util.randint(1, 20);																	// secondary corridors
			vectors[idx][10] = ""+Util.randint(0, 500);																	// secondary corridors length max
			vectors[idx][11] = ""+Util.randint(0, Integer.parseInt( vectors[idx][10] ));// secondary corridors length min
			vectors[idx][12] = ""+Util.randint(0, 30);																	// loops count
			vectors[idx][13] = ""+Util.randint(0, 50);																	// room sparsity
		}

		int count = 0;
		HashSet<String[]> failed = new HashSet<>();
		for ( String[] vector : vectors ) {
			mapGen.setConfig( vector );
			mapGen.generate();
			int[][] map = mapGen.getMap();
//			count += testMapConnectivity( map ) ? 1 : 0 ;
			if ( testMapConnectivity(map) )
				count++;
			else
				failed.add(vector);
		}
		for ( String[] faild : failed.toArray(new String[failed.size()][]) ) {
			Util.sop(Arrays.toString(faild));
		}
		// passed if 99% + of generated maps are valid
		if ( count <= vectors.length * 0.99 )
			fail("generated " +count + "/" + vectors.length);
		Util.sop("generated " +count + "/" + vectors.length);
	}
}
