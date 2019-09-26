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
public class StatGridTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(StatGridTest.class);
	}

	public void test1() throws Exception {
		StatGrid sg = new StatGrid();

		assertEquals(3035, sg.getEPSGCode());
		assertEquals(100000.0, sg.getResolution());
		assertEquals(0.0, sg.getToleranceDistance());
		assertEquals(StatGrid.GridCellGeometryType.SURFACE, sg.getGridCellGeometryType());
		assertEquals(10201, sg.getCells().size());
	}

	public void test2() throws Exception {
		String id;
		id = StatGrid.getGridCellId(5248, 1000.0, new Coordinate(14645, 165184));
		assertEquals("CRS5248RES1000mN14645E165184", id);
		id = StatGrid.getGridCellId(481, 1547.0, new Coordinate(5215214, 1512124));
		assertEquals("CRS481RES1547mN5215214E1512124", id);
	}

}
