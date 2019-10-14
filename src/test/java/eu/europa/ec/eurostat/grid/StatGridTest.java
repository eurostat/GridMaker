/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import org.locationtech.jts.geom.Coordinate;

import eu.europa.ec.eurostat.grid.GridCell.GridCellGeometryType;
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

		//SHPUtil.saveSHP(sg.getCells(), "C:/Users/gaffuju/Desktop/test.shp", CRS.decode("EPSG:3035"));

		assertEquals("3035", sg.getEPSGCode());
		assertEquals(100000.0, sg.getResolution());
		assertEquals(0.0, sg.getToleranceDistance());
		assertEquals(GridCellGeometryType.SURFACE, sg.getGridCellGeometryType());
		assertEquals(10201, sg.getCells().size());
	}

	public void test2() throws Exception {
		StatGrid sg = new StatGrid();
		sg.setEPSGCode("1464412");
		sg.setResolution(50000);
		sg.setToleranceDistance(500000.0);
		sg.setGridCellGeometryType(GridCellGeometryType.CENTER_POINT);

		//SHPUtil.saveSHP(sg.getCells(), "C:/Users/gaffuju/Desktop/test.shp", CRS.decode("EPSG:3035"));

		assertEquals("1464412", sg.getEPSGCode());
		assertEquals(50000.0, sg.getResolution());
		assertEquals(500000.0, sg.getToleranceDistance());
		assertEquals(GridCellGeometryType.CENTER_POINT, sg.getGridCellGeometryType());
		assertEquals(48748, sg.getCells().size());
	}

	public void test3() throws Exception {
		String id;
		id = StatGrid.getGridCellId("5248", 1000.0, new Coordinate(14645, 165184));
		assertEquals("CRS5248RES1000mN14645E165184", id);
		id = StatGrid.getGridCellId("481", 1547.0, new Coordinate(5215214, 1512124));
		assertEquals("CRS481RES1547mN5215214E1512124", id);
	}

}
