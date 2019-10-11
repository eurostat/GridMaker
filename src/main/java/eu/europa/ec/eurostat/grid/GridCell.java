/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import org.locationtech.jts.geom.Coordinate;

/**
 * @author Julien Gaffuri
 *
 */
public class GridCell {

	public GridCell(String gridCellId) {
		this.gridCellId = gridCellId;
	}

	public GridCell(String epsgCode, int gridResolution, int lowerLeftCornerPositionX, int lowerLeftCornerPositionY) {
		this.epsgCode = epsgCode;
		this.gridResolution = gridResolution;
		//TODO handle case of geographic coordinates
		this.lowerLeftCornerPosition = new Coordinate(lowerLeftCornerPositionX, lowerLeftCornerPositionY);
	}


	private String gridCellId = null;
	public String getGridCellId() {
		if(gridCellId==null) {
			gridCellId = getGridCellId(epsgCode, gridResolution, lowerLeftCornerPosition);
		}
		return gridCellId;
	}

	private String epsgCode = null;
	public String getEpsgCode() {
		return epsgCode;
	}

	private int gridResolution = -1;
	public int getGridResolution() {
		return gridResolution;
	}

	private Coordinate lowerLeftCornerPosition = null;




	/**
	 * Build a cell code (according to INSPIRE coding system, @see https://inspire.ec.europa.eu/id/document/tg/su).
	 * This is valid only for a grids in a cartographic projection.
	 * Examples:
	 * - CRS3035RES200mN1453400E1452800
	 * - CRS3035RES100000mN5400000E1200000
	 * 
	 * @param epsgCode
	 * @param gridResolutionM
	 * @param lowerLeftCornerPosition NB: The coordinates are supposed to be integer
	 * @return
	 */
	public static String getGridCellId(String epsgCode, int gridResolutionM, Coordinate lowerLeftCornerPosition) {
		return 
				"CRS"+epsgCode
				+"RES"+Integer.toString((int)gridResolutionM)+"m"
				+"N"+Integer.toString((int)lowerLeftCornerPosition.getX())
				+"E"+Integer.toString((int)lowerLeftCornerPosition.getY())
				;
	}

}
