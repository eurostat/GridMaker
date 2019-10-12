/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import eu.europa.ec.eurostat.grid.utils.Feature;

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
		if(gridCellId==null)
			gridCellId = getGridCellId(epsgCode, gridResolution, lowerLeftCornerPosition);
		return gridCellId;
	}

	private String epsgCode = null;
	public String getEpsgCode() {
		if(epsgCode == null) parseGridCellId();
		return epsgCode;
	}

	private int gridResolution = -1;
	public int getGridResolution() {
		if(gridResolution == -1) parseGridCellId();
		return gridResolution;
	}

	private Coordinate lowerLeftCornerPosition = null;
	public int getLowerLeftCornerPositionX() {
		if(lowerLeftCornerPosition == null) parseGridCellId();
		return lowerLeftCornerPosition.X;
	}
	public int getLowerLeftCornerPositionY() {
		if(lowerLeftCornerPosition == null) parseGridCellId();
		return lowerLeftCornerPosition.Y;
	}


	private void parseGridCellId() {
		//TODO
	}


	public Envelope getEnvelope() {
		int x = getLowerLeftCornerPositionX();
		int y = getLowerLeftCornerPositionY();
		return new Envelope(x, x+getGridResolution(), y, y+getGridResolution());
	}

	//build polygon geometry
	public Polygon getPolygonGeometry(GeometryFactory gf) {
		int x = getLowerLeftCornerPositionX();
		int y = getLowerLeftCornerPositionY();
		int res = getGridResolution();
		Coordinate[] cs = new Coordinate[]{new Coordinate(x,y), new Coordinate(x+res,y), new Coordinate(x+res,y+res), new Coordinate(x,y+res), new Coordinate(x,y)};
		return gf.createPolygon(cs);
	}

	//build point geometry
	public Point getPointGeometry(GeometryFactory gf) {
		int x = getLowerLeftCornerPositionX();
		int y = getLowerLeftCornerPositionY();
		return gf.createPoint(new Coordinate(x+0.5*getGridResolution(), y+0.5*getGridResolution()));
	}

	public Feature toFeature() {
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


	public static boolean contains(String gridCellId1, String gridCellId2) {
		//TODO
		return false;
	}

}
