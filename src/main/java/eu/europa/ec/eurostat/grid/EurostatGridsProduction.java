/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;

import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;
import eu.europa.ec.eurostat.grid.utils.SHPUtil;

/**
 * @author julien Gaffuri
 *
 */
public class EurostatGridsProduction {
	static Logger logger = Logger.getLogger(EurostatGridsProduction.class.getName());



	public static void main(String[] args) throws Exception {
		logger.info("Start");

		logger.setLevel(Level.ALL);
		StatGridCountryUtil.logger.setLevel(Level.ALL);

		String path = "C:/Users/gaffuju/Desktop/grid/";
		Geometry europeGeom = CountriesUtil.getEurope();
		ArrayList<Feature> cnts = CountriesUtil.getEuropeanCountries();

		//build pan-European grids for various resolutions
		for(int resKM : new int[] {100,50,10,5}) {

			logger.info("Make " + resKM + "km grid...");
			Collection<Feature> cells = StatGridCountryUtil.proceed(resKM*1000.0, 3035, europeGeom, 1000, "CNTR_ID", cnts, "CNTR_ID");

			logger.info("Save " + cells.size() + " cells...");
			SHPUtil.saveSHP(cells, path+resKM+"km/grid_"+resKM+"km.shp", CRS.decode("EPSG:3035"));
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
		Geometry cntGeom = CountriesUtil.getEuropeanCountry(countryCode).getDefaultGeometry();

		//build cells
		StatGrid grid = new StatGrid()
				.setResolution(gridResolutionM)
				.setEPSGCode(3035)
				.setGeometryToCover(cntGeom)
				.setToleranceDistance(toleranceDistance)
				;
		Collection<Feature> cells = grid.getCells();

		//set country code to cells
		for(Feature cell : cells) cell.setAttribute("CNTR_ID", countryCode);

		return cells;
	}

}
