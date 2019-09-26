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
public class Main {
	static Logger logger = Logger.getLogger(Main.class.getName());

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
			Collection<Feature> cells = EuroGridBuilder.buildGridCellsByCountry(countryCode, 1000, 500);
			logger.info("Save " + cells.size() + " cells...");
			SHPUtil.saveSHP(cells, path+"1km/grid_1km_"+countryCode+".shp", CRS.decode("EPSG:3035"));
		}

		logger.info("End");
	}

}
