/**
 * 
 */
package eu.europa.ec.eurostat.grid.examples;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;

/**
 * @author Julien Gaffuri
 *
 */
public class EurostatGridCellUtil {

	private class GridCell {
		
		private String code = null;
		private int EPSG = -1;
		private int resolution = -1;
		private Coordinate a;

		public GridCell(String code) {
			this.code = code;
		}
	}

	public static boolean contains(String gridCellId1, String gridCellId2) {
		//TODO
		return false;
	}

	public static Polygon getGeometry(String cellCode) {
		//TODO
		return null;
	}

	public static Envelope getEnvelope(String cellCode) {
		//TODO
		return null;
	}

	
}
