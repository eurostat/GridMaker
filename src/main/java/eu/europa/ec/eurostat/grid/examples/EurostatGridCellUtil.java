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





		public GridCell(String code) {
			this.gridCellId = gridCellId;
		}

		public GridCell(int epsgCode, int gridResolution, int lowerLeftCornerPositionX, int lowerLeftCornerPositionY) {
			this.epsgCode = epsgCode;
			this.gridResolution = gridResolution;
			//TODO handle case of geographic coordinates
			this.lowerLeftCornerPosition = new Coordinate(lowerLeftCornerPositionX, lowerLeftCornerPositionY);
		}


		private String gridCellId = null;
		public String getGridCellId() {
			return gridCellId;
		}

		private int epsgCode = -1;
		public int getEpsgCode() {

			return epsgCode;
		}

		private int gridResolution = -1;
		public int getGridResolution() {
			return gridResolution;
		}

		private Coordinate lowerLeftCornerPosition = null;

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


	/**
	 * Build a cell code (according to INSPIRE coding system, @see https://inspire.ec.europa.eu/id/document/tg/su).
	 * This is valid only for a grids in a cartographic projection.
	 * Examples:
	 * - CRS3035RES200mN1453400E1452800
	 * - CRS3035RES100000mN5400000E1200000
	 * 
	 * @param epsgCode
	 * @param gridResolutionM
	 * @param lowerLeftCornerPosition
	 * @return
	 */
	public static String getGridCellId(String epsgCode, double gridResolutionM, Coordinate lowerLeftCornerPosition) {
		return 
				"CRS"+epsgCode
				+"RES"+Integer.toString((int)gridResolutionM)+"m"
				+"N"+Integer.toString((int)lowerLeftCornerPosition.getX())
				+"E"+Integer.toString((int)lowerLeftCornerPosition.getY())
				;
	}


}
