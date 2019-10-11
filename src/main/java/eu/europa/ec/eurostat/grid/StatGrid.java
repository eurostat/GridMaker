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
 * The resolution, coordinate reference system, extent and cell geometry types can be defined by th users.
 * Both cartographic and geographical grids are supported.
 * 
 * @author julien Gaffuri
 *
 */
public class StatGrid {
	public static Logger logger = Logger.getLogger(StatGrid.class.getName());

	/**
	 * The grid resolution (pixel size).
	 * NB: The unit of measure should be the same as the one of the Coordinate Reference System.
	 */
	private int resolution = 100000;
	public double getResolution() { return resolution; }
	public StatGrid setResolution(int resolution) {
		this.resolution = resolution;
		cells = null;
		return this;
	}

	/**
	 * The EPSG code of the Coordinate Reference System of the grid.
	 * @see https://spatialreference.org/ref/epsg/
	 */
	private String epsgCode = "3035";
	public String getEPSGCode() { return epsgCode; }
	public StatGrid setEPSGCode(String epsgCode) {
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
			geometryToCover = getGeometry(new Envelope(0.0, 10000000.0, 0.0, 10000000.0), new GeometryFactory());
		return geometryToCover;
	}
	public StatGrid setGeometryToCover(Geometry geometryToCover) {
		this.geometryToCover = geometryToCover;
		cells = null;
		return this;
	}
	public StatGrid setGeometryToCover(Envelope envelopeToCover) {
		return setGeometryToCover(getGeometry(envelopeToCover, new GeometryFactory()));
	}

	/**
	 * All cells within this tolerance distance to 'geometryToCover' will be included in the grid.
	 * NB 1: The unit of measure should be the same as the one of the Coordinate Reference System.
	 * NB 2: This distance can be negative.
	 */
	private double toleranceDistance = 0.0;
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
		GeometryFactory gf = getGeometryToCover().getFactory();

		//get geometry to cover
		Geometry geomCovBuff = getGeometryToCover();

		if( toleranceDistance != 0 ) {
			if(logger.isDebugEnabled()) logger.debug("   (make buffer...)");
			geomCovBuff = getGeometryToCover().buffer(toleranceDistance);
		}

		//get envelope to cover
		Envelope envCovBuff = geomCovBuff.getEnvelopeInternal();
		envCovBuff = ensureGrid(envCovBuff, resolution);

		cells = new ArrayList<Feature>();
		for(double x=envCovBuff.getMinX(); x<envCovBuff.getMaxX(); x += resolution)
			for(double y=envCovBuff.getMinY(); y<envCovBuff.getMaxY(); y += resolution) {

				//build cell envelope
				Envelope gridCellEnv = new Envelope(x, x+resolution, y, y+resolution);
				//check intersection with envCovBuff
				if( ! envCovBuff.intersects(gridCellEnv) ) continue;

				//build cell geometry
				Geometry gridCellGeom = getGeometry(gridCellEnv, gf);
				//check intersection with geometryToCover
				if( ! geomCovBuff.intersects(gridCellGeom) ) continue;

				//build the cell
				Feature cell = new Feature();

				//set geometry
				if(gridCellGeometryType == GridCellGeometryType.CENTER_POINT)
					gridCellGeom = gridCellGeom.getCentroid();
				cell.setDefaultGeometry(gridCellGeom);

				//set id
				String id = GridCell.getGridCellId(epsgCode, resolution, new Coordinate(x,y));
				cell.setID(id);
				cell.setAttribute("cellId", id);

				cells.add(cell);
			}
		if(logger.isDebugEnabled()) logger.debug(cells.size() + " cells built");
		return this;
	}






	private static Envelope ensureGrid(Envelope env, double res) {
		double xMin = env.getMinX() - env.getMinX()%res;
		double xMax = (1+(int)(env.getMaxX()/res))*res;
		double yMin = env.getMinY() - env.getMinY()%res;
		double yMax = (1+(int)(env.getMaxY()/res))*res;
		return new Envelope(xMin, xMax, yMin, yMax);
	}

	//build geometry from envelope
	private static Polygon getGeometry(Envelope env, GeometryFactory gf) {
		Coordinate[] cs = new Coordinate[]{new Coordinate(env.getMinX(),env.getMinY()), new Coordinate(env.getMaxX(),env.getMinY()), new Coordinate(env.getMaxX(),env.getMaxY()), new Coordinate(env.getMinX(),env.getMaxY()), new Coordinate(env.getMinX(),env.getMinY())};
		return gf.createPolygon(cs);
	}

}
