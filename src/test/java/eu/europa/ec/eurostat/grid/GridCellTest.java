/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;

/**
 * @author Julien Gaffuri
 *
 */
public class GridCellTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(GridCellTest.class);
	}



	public void test1() throws Exception {
		String id;
		id = GridCell.getGridCellId("5248", 1000, new Coordinate(14645, 165184));
		assertEquals("CRS5248RES1000mN14645E165184", id);
		id = GridCell.getGridCellId("481", 1547, new Coordinate(5215214, 1512124));
		assertEquals("CRS481RES1547mN5215214E1512124", id);
	}

}
