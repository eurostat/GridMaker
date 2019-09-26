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
 * The resolution, coordinate reference system,
 * 
 * @author julien Gaffuri
 *
 */
public class StatGrid {
	static Logger logger = Logger.getLogger(StatGrid.class.getName());

	/**
	 * The grid resolution.
	 * NB: The unit of measure should be the same as the one of the Coordinate Reference System.
	 */
	private double resolution = 100000;
	public double getResolution() { return resolution; }
	public StatGrid setResolution(double resolution) {
		this.resolution = resolution;
		cells = null;
		return this;
	}

	/**
	 * The EPSG code of the Coordinate Reference System of the grid.
	 * @see https://spatialreference.org/ref/epsg/
	 */
	private int epsgCode = 3035;
	public int getEPSGCode() { return epsgCode; }
	public StatGrid setEPSGCode(int epsgCode) {
		this.epsgCode = epsgCode;
		cells = null;
		return this;
	}

	/**
	 * The geometry the grid should cover, taking into account also the 'toleranceDistance' parameter.
	 * NB: Of course, the geometry should be defined in the Coordinate Reference System of the grid.
	 */
	private Geometry geometryToCover;
	public Geometry getGeometryToCover() {
		if(geometryToCover == null)
			geometryToCover = getGeometry(new Envelope(0.0, 10000000.0, 0.0, 10000000.0));
		return geometryToCover;
	}
	public StatGrid setGeometryToCover(Geometry geometryToCover) {
		this.geometryToCover = geometryToCover;
		cells = null;
		return this;
	}
	public StatGrid setGeometryToCover(Envelope envelopeToCover) {
		return setGeometryToCover(getGeometry(envelopeToCover));
	}

	/**
	 * All cells within this tolerance distance to 'geometryToCover' will be included in the grid.
	 * NB: The unit of measure should be the same as the one of the Coordinate Reference System.
	 */
	private double toleranceDistance = 0;
	public double getToleranceDistance() { return toleranceDistance; }
	public StatGrid setToleranceDistance(double toleranceDistance) {
		this.toleranceDistance = toleranceDistance;
		cells = null;
		return this;
	}

	/**
	 * The type of grid cell geometry: The surface representation (a square) or its center point.
	 * 
	 * @author Julien Gaffuri
	 */
	public static enum GridCellGeometryType {SURFACE, CENTER_POINT};

	/**
	 * The grid cell geometry type.
	 * @see GridCellGeometryType
	 */
	private GridCellGeometryType gridCellGeometryType = GridCellGeometryType.SURFACE;
	public GridCellGeometryType getGridCellGeometryType() { return gridCellGeometryType; }
	public StatGrid setGridCellGeometryType(GridCellGeometryType geomType) {
		this.gridCellGeometryType = geomType;
		cells = null;
		return this;
	}

	/**
	 * The grid cells.
	 */
	private Collection<Feature> cells = null;
	public Collection<Feature> getCells() {
		if(cells == null) buildCells();
		return cells;
	}



	/**
	 * Build the grid cells.
	 * 
	 * @return this object
	 */
	private StatGrid buildCells() {
		if(logger.isDebugEnabled()) logger.debug("Build grid cells...");

		//get grid envelope
		Envelope env = ensureGrid(getGeometryToCover().getEnvelopeInternal(), resolution);

		//get envelop to cover
		Envelope envCover = geometryToCover.getEnvelopeInternal();
		envCover.expandBy(toleranceDistance*1.0001);

		cells = new ArrayList<Feature>();
		for(double x=env.getMinX(); x<env.getMaxX(); x += resolution)
			for(double y=env.getMinY(); y<env.getMaxY(); y += resolution) {

				//build cell polygon geometry
				Geometry gridCellGeom = createPolygon( x,y, x+resolution,y, x+resolution,y+resolution, x,y+resolution, x,y );

				//check distance to geometryToCover
				if(!gridCellGeom.getEnvelopeInternal().intersects(envCover)) continue;
				if(gridCellGeom.distance(geometryToCover) > toleranceDistance) continue;

				//build the cell
				Feature cell = new Feature();

				//set geometry
				if(gridCellGeometryType == GridCellGeometryType.CENTER_POINT)
					gridCellGeom = gridCellGeom.getCentroid();
				cell.setDefaultGeometry(gridCellGeom);

				//set id
				String id = getGridCellId(epsgCode, resolution, new Coordinate(x,y));
				cell.setID(id);
				cell.setAttribute("cellId", id);

				cells.add(cell);
			}
		if(logger.isDebugEnabled()) logger.debug(cells.size() + " cells built");
		return this;
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

	private static Envelope ensureGrid(Envelope env, double res) {
		double xMin = env.getMinX() - env.getMinX()%res;
		double xMax = (1+(int)(env.getMaxX()/res))*res;
		double yMin = env.getMinY() - env.getMinY()%res;
		double yMax = (1+(int)(env.getMaxY()/res))*res;
		return new Envelope(xMin, xMax, yMin, yMax);
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
