/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;

import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;
import eu.europa.ec.eurostat.grid.utils.SHPUtil;

/**
 * @author julien Gaffuri
 *
 */
public class EuroGridBuilder {
	static Logger logger = Logger.getLogger(EuroGridBuilder.class.getName());

	/**
	 * Build grid cells covering a geometry.
	 * 
	 * @param geometryToCover
	 * @param gridResolutionM
	 * @param epsgCode
	 * @return
	 */
	public static Collection<Feature> buildGridCells(Geometry geometryToCover, double gridResolutionM, int epsgCode) {
		if(logger.isDebugEnabled()) logger.debug("Build grid cells...");

		//get grid envelope
		Envelope env = ensureGrid(geometryToCover.getEnvelopeInternal(), gridResolutionM);

		Collection<Feature> cells = new ArrayList<Feature>();
		for(double x=env.getMinX(); x<env.getMaxX(); x+=gridResolutionM)
			for(double y=env.getMinY(); y<env.getMaxY(); y+=gridResolutionM) {

				//build cell geometry
				Polygon gridCellGeom = createPolygon( x,y, x+gridResolutionM,y, x+gridResolutionM,y+gridResolutionM, x,y+gridResolutionM, x,y );

				//check intersection with geometryToCover
				if(!gridCellGeom.getEnvelopeInternal().intersects(geometryToCover.getEnvelopeInternal())) continue;
				if(!gridCellGeom.intersects(geometryToCover)) continue;

				//build and keep the cell
				Feature cell = new Feature();
				cell.setDefaultGeometry(gridCellGeom);
				cell.setID( getGridCellId(epsgCode, gridResolutionM, new Coordinate(x,y)) );
				cell.setAttribute("cellId", cell.getID());
				cells.add(cell);
			}
		if(logger.isDebugEnabled()) logger.debug(cells.size() + " cells built");
		return cells;
	}



	/**
	 * Assign grid cells to countries.
	 * If a grid cell intersects the bufferred geometry of a country, then an attribute of the cell is assigned with this country code.
	 * For cells that are to be assigned to several countries, several country codes are assigned.
	 * 
	 * @param cells
	 * @param cntStampAtt
	 * @param countries
	 * @param cntBufferDist
	 * @param cntIdAtt
	 */
	public static void addCountryStamp(Collection<Feature> cells, String cntStampAtt, Collection<Feature> countries, double cntBufferDist, String cntIdAtt) {
		if(logger.isDebugEnabled()) logger.debug("Stamp country...");

		//initialise cell country stamp
		for(Feature cell : cells)
			cell.setAttribute(cntStampAtt, "");

		//index cells
		STRtree index = new STRtree();
		for(Feature cell : cells)
			index.insert(cell.getDefaultGeometry().getEnvelopeInternal(), cell);

		for(Feature cnt : countries) {
			//get cnt geometry and code
			Geometry cntGeom = cnt.getDefaultGeometry();
			String cntCode = cnt.getAttribute(cntIdAtt).toString();

			//apply buffer
			if(cntBufferDist >= 0)
				cntGeom = cntGeom.buffer(cntBufferDist);

			//get grid cells intersecting
			Envelope cntEnv = cntGeom.getEnvelopeInternal();
			for(Object cell_ : index.query(cntEnv)) {
				Feature cell = (Feature)cell_;
				if(!cntEnv.intersects(cell.getDefaultGeometry().getEnvelopeInternal())) continue;
				if(!cntGeom.intersects(cell.getDefaultGeometry())) continue;
				String csa = cell.getAttribute(cntStampAtt).toString();
				if("".equals(csa))
					cell.setAttribute(cntStampAtt, cntCode);
				else
					cell.setAttribute(cntStampAtt, csa+"-"+cntCode);
			}
		}

	}

	/**
	 * Remove cells which are not assigned to any country,
	 * that is the ones with attribute 'cntStampAtt' null or set to "".
	 * 
	 * @param cells
	 * @param cntStampAtt
	 */
	public static void filterCountryStamp(Collection<Feature> cells, String cntStampAtt) {
		if(logger.isDebugEnabled()) logger.debug("Filtering...");
		Collection<Feature> toRemove = new ArrayList<Feature>();
		for(Feature cell : cells) {
			Object cellCnt = cell.getAttribute(cntStampAtt);
			if(cellCnt==null || cellCnt.toString().equals("")) toRemove.add(cell);
		}
		cells.removeAll(toRemove);
		if(logger.isDebugEnabled()) logger.debug(toRemove.size() + " cells to remove. " + cells.size() + " cells left");
	}



	//sequencing
	public static Collection<Feature> proceed(Geometry geometryToCover, double res, int epsg, String cntStampAtt, Collection<Feature> countries, double cntBufferDist, String cntIdAtt) {
		Collection<Feature> cells = EuroGridBuilder.buildGridCells(geometryToCover, res, epsg);
		EuroGridBuilder.addCountryStamp(cells, cntStampAtt, countries, cntBufferDist, cntIdAtt);
		EuroGridBuilder.filterCountryStamp(cells, cntStampAtt);
		return cells;
	}


	/**
	 * Build grid covering a single country.
	 * In EPSG 3035 only.
	 * 
	 * @param countryCode
	 * @param gridResolutionM
	 * @param cntBufferDist
	 * @return
	 */
	public static Collection<Feature> buildGridCellsByCountry(String countryCode, double gridResolutionM, double cntBufferDist) {
		//get country geometry
		Geometry cntGeom = CountriesUtil.getCountry(countryCode).getDefaultGeometry();
		if(cntBufferDist >= 0)
			cntGeom = cntGeom.buffer(cntBufferDist);
		//build cells
		Collection<Feature> cells = buildGridCells(cntGeom, gridResolutionM, 3035);
		//set cnt code to cells
		for(Feature cell : cells) cell.setAttribute("CNTR_ID", countryCode);
		return cells;
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


	public static void main(String[] args) throws Exception {
		logger.info("Start");

		logger.setLevel(Level.ALL);
		EuroGridBuilder.logger.setLevel(Level.ALL);

		String path = "C:/Users/gaffuju/Desktop/grid/";
		Geometry area = CountriesUtil.getEuropeMask();
		ArrayList<Feature> cnts = CountriesUtil.getEuropeanCountries();

		//build pan-European grids
		for(int res : new int[] {100,50,10,5}) {
			logger.info("Make "+res+"km grid...");
			Collection<Feature> cells = EuroGridBuilder.proceed(area, res*1000, 3035, "CNTR_ID", cnts, 1000, "CNTR_ID");
			logger.info("Save " + cells.size() + " cells...");
			SHPUtil.saveSHP(cells, path+res+"km/grid_"+res+"km.shp", CRS.decode("EPSG:3035"));
		}

		//build country 1km grids by country
		for(String countryCode : CountriesUtil.EuropeanCountryCodes) {
			logger.info("Make 1km grid for "+countryCode+"...");
			Collection<Feature> cells = buildGridCellsByCountry(countryCode, 1000, 500);
			logger.info("Save " + cells.size() + " cells...");
			SHPUtil.saveSHP(cells, path+"1km/grid_1km_"+countryCode+".shp", CRS.decode("EPSG:3035"));
		}

		logger.info("End");
	}

}
