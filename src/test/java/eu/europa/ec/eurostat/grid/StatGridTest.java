/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import junit.framework.TestCase;

/**
 * @author Julien Gaffuri
 *
 */
public class StatGridTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(StatGridTest.class);
	}

	public void test1() {
		StatGrid sg = new StatGrid();

		assertEquals(3035, sg.getEPSGCode());
		assertEquals(100000.0, sg.getResolution());
		assertEquals(0.0, sg.getToleranceDistance());
		assertEquals(StatGrid.GridCellGeometryType.SURFACE, sg.getGridCellGeometryType());
		assertEquals(10000, sg.getCells().size());
	}

}
