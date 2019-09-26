/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;

/**
 * @author julien Gaffuri
 *
 */
public class EuroGridBuilder {
	static Logger logger = Logger.getLogger(EuroGridBuilder.class.getName());



	/**
	 * Assign grid cells to countries.
	 * If a grid cell intersects or is nearby the geometry of a country, then an attribute of the cell is assigned with this country code.
	 * For cells that are to be assigned to several countries, several country codes are assigned.
	 * 
	 * @param cells
	 * @param cellCountryAttribute
	 * @param countries
	 * @param toleranceDistance
	 * @param countryIdAttribute
	 */
	public static void assignCountries(Collection<Feature> cells, String cellCountryAttribute, Collection<Feature> countries, double toleranceDistance, String countryIdAttribute) {
		if(logger.isDebugEnabled()) logger.debug("Assign country...");

		//initialise cell country attribute
		for(Feature cell : cells)
			cell.setAttribute(cellCountryAttribute, "");

		//index cells
		STRtree index = new STRtree();
		for(Feature cell : cells)
			index.insert(cell.getDefaultGeometry().getEnvelopeInternal(), cell);

		for(Feature cnt : countries) {
			//get cnt geometry and code
			Geometry cntGeom = cnt.getDefaultGeometry();
			String cntCode = cnt.getAttribute(countryIdAttribute).toString();

			//get country envelope
			Envelope cntEnv = cntGeom.getEnvelopeInternal();
			cntEnv.expandBy(toleranceDistance);

			//get grid cells intersecting
			for(Object cell_ : index.query(cntEnv)) {
				Feature cell = (Feature)cell_;
				Geometry cellGeom = cell.getDefaultGeometry();

				if(!cellGeom.getEnvelopeInternal().intersects(cntEnv)) continue;
				if(cellGeom.distance(cntGeom) > toleranceDistance) continue;

				String csa = cell.getAttribute(cellCountryAttribute).toString();
				if("".equals(csa))
					cell.setAttribute(cellCountryAttribute, cntCode);
				else
					cell.setAttribute(cellCountryAttribute, csa+"-"+cntCode);
			}
		}

	}


	/**
	 * Remove cells which are not assigned to any country,
	 * that is the ones with attribute 'cellCountryAttribute' null or set to "".
	 * 
	 * @param cells
	 * @param cellCountryAttribute
	 */
	public static void filterCellsWithoutCountry(Collection<Feature> cells, String cellCountryAttribute) {
		if(logger.isDebugEnabled()) logger.debug("Filtering...");
		Collection<Feature> toRemove = new ArrayList<Feature>();
		for(Feature cell : cells) {
			Object cellCnt = cell.getAttribute(cellCountryAttribute);
			if(cellCnt==null || cellCnt.toString().equals("")) toRemove.add(cell);
		}
		cells.removeAll(toRemove);
		if(logger.isDebugEnabled()) logger.debug(toRemove.size() + " cells to remove. " + cells.size() + " cells left");
	}



	//sequencing
	public static Collection<Feature> proceed(Geometry geometryToCover, double res, int epsgCode, String cellCountryAttribute, Collection<Feature> countries, double toleranceDistance, String cntIdAtt) {
		StatGrid grid = new StatGrid()
				.setGeometryToCover(geometryToCover)
				.setResolution(res)
				.setEPSGCode(epsgCode)
				;
		Collection<Feature> cells = grid.getCells();

		EuroGridBuilder.assignCountries(cells, cellCountryAttribute, countries, toleranceDistance, cntIdAtt);
		EuroGridBuilder.filterCellsWithoutCountry(cells, cellCountryAttribute);
		return cells;
	}


	/**
	 * Build grid covering a single country.
	 * In EPSG 3035 only.
	 * 
	 * @param countryCode
	 * @param gridResolutionM
	 * @param toleranceDistance
	 * @return
	 */
	public static Collection<Feature> buildGridCellsByCountry(String countryCode, double gridResolutionM, double toleranceDistance) {

		//get country geometry
		Geometry cntGeom = CountriesUtil.getCountry(countryCode).getDefaultGeometry();
		if(toleranceDistance >= 0)
			cntGeom = cntGeom.buffer(toleranceDistance);

		//build cells
		StatGrid grid = new StatGrid()
				.setGeometryToCover(cntGeom)
				.setResolution(gridResolutionM)
				.setEPSGCode(3035)
				;
		Collection<Feature> cells = grid.getCells();

		//set cnt code to cells
		for(Feature cell : cells) cell.setAttribute("CNTR_ID", countryCode);

		return cells;
	}

}
