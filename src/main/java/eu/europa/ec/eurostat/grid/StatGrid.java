/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import eu.europa.ec.eurostat.grid.utils.Feature;

/**
 * Build a statistical grid.
 * 
 * @author julien Gaffuri
 *
 */
public class StatGrid {
	static Logger logger = Logger.getLogger(StatGrid.class.getName());

	private double resolutionM = 100000;
	public double getResolutionM() { return resolutionM; }
	public StatGrid setResolutionM(double resolutionM) { this.resolutionM = resolutionM; cells=null; return this; }

	private int epsgCode = 3035;
	public int getEpsgCode() { return epsgCode; }
	public StatGrid setEpsgCode(int epsgCode) { this.epsgCode = epsgCode; cells=null; return this; }

	private Geometry geometryToCover;
	public Geometry getGeometryToCover() { return geometryToCover; }
	public StatGrid setGeometryToCover(Geometry geometryToCover) { this.geometryToCover = geometryToCover; cells=null; return this; }
	public StatGrid setGeometryToCover(Envelope envelopeToCover) { return setGeometryToCover(getGeometry(envelopeToCover)); }

	public static enum GridCellGeometryType {SURFACE, CENTER_POINT};
	private GridCellGeometryType gridCellGeometryType = GridCellGeometryType.SURFACE;
	public GridCellGeometryType getGridCellGeometryType() { return gridCellGeometryType; }
	public StatGrid setGridCellGeometryType(GridCellGeometryType geomType) { this.gridCellGeometryType = geomType; cells=null; return this; }

	private Collection<Feature> cells = null;
	public Collection<Feature> getCells() {
		if(cells == null) buildCells();
		return cells;
	}



	public void buildCells() {
		if(logger.isDebugEnabled()) logger.debug("Build grid cells...");

		//get grid envelope
		Envelope env = ensureGrid(geometryToCover.getEnvelopeInternal(), getResolutionM());

		cells = new ArrayList<Feature>();
		for(double x=env.getMinX(); x<env.getMaxX(); x+=getResolutionM())
			for(double y=env.getMinY(); y<env.getMaxY(); y+=getResolutionM()) {

				//build cell geometry
				Polygon gridCellGeom = createPolygon( x,y, x+getResolutionM(),y, x+getResolutionM(),y+getResolutionM(), x,y+getResolutionM(), x,y );

				//check intersection with geometryToCover
				if(!gridCellGeom.getEnvelopeInternal().intersects(geometryToCover.getEnvelopeInternal())) continue;
				if(!gridCellGeom.intersects(geometryToCover)) continue;

				//build and keep the cell
				Feature cell = new Feature();
				cell.setDefaultGeometry(gridCellGeom); //TODO depends on geomType
				cell.setID( getGridCellId(epsgCode, getResolutionM(), new Coordinate(x,y)) );
				cell.setAttribute("cellId", cell.getID());
				cells.add(cell);
			}
		if(logger.isDebugEnabled()) logger.debug(cells.size() + " cells built");
	}









	private static Envelope ensureGrid(Envelope env, double res) {
		double xMin = env.getMinX() - env.getMinX()%res;
		double xMax = (1+(int)(env.getMaxX()/res))*res;
		double yMin = env.getMinY() - env.getMinY()%res;
		double yMax = (1+(int)(env.getMaxY()/res))*res;
		return new Envelope(xMin, xMax, yMin, yMax);
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
	public static String getGridCellId(int epsgCode, double gridResolutionM, Coordinate lowerLeftCornerPosition) {
		return 
				"CRS"+Integer.toString((int)epsgCode)
				+"RES"+Integer.toString((int)gridResolutionM)+"m"
				+"N"+Integer.toString((int)lowerLeftCornerPosition.getX())
				+"E"+Integer.toString((int)lowerLeftCornerPosition.getY())
				;
	}

	private static Coordinate[] createCoordinates(double... cs) {
		Coordinate[] cs_ = new Coordinate[cs.length/2];
		for(int i=0; i<cs_.length; i++) cs_[i] = new Coordinate(cs[2*i],cs[2*i+1]);
		return cs_;
	}
	private static Polygon createPolygon(double... cs) { return new GeometryFactory().createPolygon(createCoordinates(cs)); }

	//build geometry from envelope
	private static Polygon getGeometry(Envelope env) {
		Coordinate[] cs = new Coordinate[]{new Coordinate(env.getMinX(),env.getMinY()), new Coordinate(env.getMaxX(),env.getMinY()), new Coordinate(env.getMaxX(),env.getMaxY()), new Coordinate(env.getMinX(),env.getMaxY()), new Coordinate(env.getMinX(),env.getMinY())};
		return new GeometryFactory().createPolygon(cs);
	}
}
