/**
 * 
 */
package eu.europa.ec.eurostat.grid.examples;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.europa.ec.eurostat.grid.StatGrid;
import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;
import eu.europa.ec.eurostat.grid.utils.SHPUtil;

/**
 * Examples to produce European grids based on ETRS89-LAEA coordinate reference system (EPSG:3035)
 * for various resolutions. The cells are tagged by country and identified with their standard code.
 * 
 * @author julien Gaffuri
 *
 */
public class EurostatGridsProduction {
	static Logger logger = Logger.getLogger(EurostatGridsProduction.class.getName());

	//use CNTR_2010_100k or GAUL
	//see also: https://www.efgs.info/data/
	//https://esdac.jrc.ec.europa.eu/content/european-reference-grids

	public static void main(String[] args) throws Exception {
		logger.info("Start");

		logger.setLevel(Level.ALL);
		StatGrid.logger.setLevel(Level.ALL);
		StatGridCountryUtil.logger.setLevel(Level.ALL);

		String path = "C:/Users/gaffuju/Desktop/grid/";
		CoordinateReferenceSystem crs = CRS.decode("EPSG:3035");



		//make pan-european grid datasets
		logger.info("Get Europe cover (buffer)...");
		Geometry europeCover = CountriesUtil.getEurope(true);
		europeCover = europeCover.buffer(2000);

		logger.info("Get European countries ...");
		ArrayList<Feature> cnts = CountriesUtil.getEuropeanCountries(true);

		//build pan-European grids for various resolutions
		for(int resKM : new int[] {100,50,20,10,5}) {
			logger.info("Make " + resKM + "km grid...");
			make(resKM, europeCover, cnts, path, crs);
		}



		//build country 1km grids by country
		for(String countryCode : CountriesUtil.EuropeanCountryCodes) {

			logger.info("Make 1km grid for "+countryCode+"...");
			Collection<Feature> cells = buildGridCellsByCountry(countryCode, true, 1000, 1000);

			logger.info("Save " + cells.size() + " cells...");
			SHPUtil.saveSHP(cells, path+"1km/grid_1km_"+countryCode+".shp", crs);
		}



		//try to make 2km for whole Europe
		//logger.info("Make " + 2 + "km grid...");
		//make(2, europeCover, cnts, path, crs);
		//try to make 1km for whole Europe
		logger.info("Make " + 1 + "km grid...");
		make(1, europeCover, cnts, path, crs);

		logger.info("End");
	}




	private static void make(int resKM, Geometry europeCover, ArrayList<Feature> cnts, String path, CoordinateReferenceSystem crs) {
		StatGrid grid = new StatGrid()
				.setResolution(resKM*1000.0)
				.setEPSGCode("3035")
				.setGeometryToCover(europeCover)
				;
		Collection<Feature> cells = grid.getCells();
		StatGridCountryUtil.assignCountries(cells, "CNTR_ID", cnts, 1000, "CNTR_ID");
		StatGridCountryUtil.filterCellsWithoutCountry(cells, "CNTR_ID");

		logger.info("Save " + cells.size() + " cells...");
		SHPUtil.saveSHP(cells, path+resKM+"km/grid_"+resKM+"km.shp", crs);
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
	public static Collection<Feature> buildGridCellsByCountry(String countryCode, boolean withOST, double gridResolutionM, double toleranceDistance) {

		//get country geometry
		Geometry cntGeom = CountriesUtil.getEuropeanCountry(countryCode, withOST).getDefaultGeometry();

		//build cells
		StatGrid grid = new StatGrid()
				.setResolution(gridResolutionM)
				.setEPSGCode("3035")
				.setGeometryToCover(cntGeom)
				.setToleranceDistance(toleranceDistance)
				;
		Collection<Feature> cells = grid.getCells();

		//set country code to cells
		for(Feature cell : cells) cell.setAttribute("CNTR_ID", countryCode);

		return cells;
	}

}
