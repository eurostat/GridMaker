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
import eu.europa.ec.eurostat.jgiscotools.datamodel.Feature;
import eu.europa.ec.eurostat.jgiscotools.io.GeoPackageUtil;
import eu.europa.ec.eurostat.jgiscotools.io.SHPUtil;

/**
 * Examples to produce European grids based on ETRS89-LAEA coordinate reference system (EPSG:3035)
 * for various resolutions. The cells are tagged by country and identified with their standard code.
 * 
 * @author julien Gaffuri
 *
 */
public class EurostatGridsProduction {
	static Logger logger = Logger.getLogger(EurostatGridsProduction.class.getName());

	static int[] resKMs = new int[] {100,50,20,10,5};

	//see also:
	//https://www.eea.europa.eu/data-and-maps/data/eea-reference-grids-2
	//https://www.efgs.info/data/
	//https://esdac.jrc.ec.europa.eu/content/european-reference-grids

	public static void main(String[] args) throws Exception {
		logger.info("Start");

		logger.setLevel(Level.ALL);
		StatGrid.logger.setLevel(Level.ALL);
		StatGridCountryUtil.logger.setLevel(Level.ALL);

		String outpath = "C:/Users/gaffuju/Desktop/grid/";
		String path = "C:/Users/gaffuju/Desktop/CNTR_100k/";
		CoordinateReferenceSystem crs = CRS.decode("EPSG:3035");
		int bufferDistance = 1500;

		//make pan-european grid datasets
		logger.info("Get Europe cover (buffer)...");
		Geometry europeCover = SHPUtil.loadSHP(path+"Europe_100K_union_buff_"+bufferDistance+"_LAEA.shp").fs.iterator().next().getDefaultGeometry();

		logger.info("Get European countries ...");
		ArrayList<Feature> cnts = SHPUtil.loadSHP(path+"CNTR_RG_100K_union_buff_"+bufferDistance+"_LAEA.shp").fs;

		//build pan-European grids for various resolutions
		for(int resKM : resKMs) {
			logger.info("Make " + resKM + "km grid...");
			make(resKM, europeCover, cnts, bufferDistance, outpath, crs, true);
		}



		/*/build country 1km grids by country
		for(String countryCode : CountriesUtil.EuropeanCountryCodes) {

			logger.info("Make 1km grid for "+countryCode+"...");

			//get country geometry (buffer)
			Geometry countryGeom = SHPUtil.loadSHP(path+"CNTR_RG_100K_union_buff_"+bufferDistance+"_LAEA.shp", CQL.toFilter("CNTR_ID = '"+countryCode+"'"))
					.fs.iterator().next().getDefaultGeometry();

			//build cells
			StatGrid grid = new StatGrid()
					.setResolution(1000)
					.setEPSGCode("3035")
					.setGeometryToCover(countryGeom)
					;
			Collection<Feature> cells = grid.getCells();

			//set country code to cells
			for(Feature cell : cells) cell.setAttribute("CNTR_ID", countryCode);

			logger.info("Save " + cells.size() + " cells as SHP...");
			SHPUtil.saveSHP(cells, outpath+"1km/grid_1km_"+countryCode+".shp", crs);
			logger.info("Save " + cells.size() + " cells as GPKG...");
			GeoPackageUtil.save(cells, outpath+"1km/grid_1km_"+countryCode+".gpkg", crs);
		}
		 */

		//2km for whole Europe
		logger.info("Make " + 2 + "km grid...");
		make(2, europeCover, cnts, bufferDistance, outpath, crs, false);
		//1km for whole Europe
		logger.info("Make " + 1 + "km grid...");
		make(1, europeCover, cnts, bufferDistance, outpath, crs, false);

		logger.info("End");
	}




	private static void make(int resKM, Geometry europeCover, ArrayList<Feature> cnts, double bufferDistance, String path, CoordinateReferenceSystem crs, boolean saveSPH) {
		//build grid
		StatGrid grid = new StatGrid()
				.setResolution(resKM*1000)
				.setEPSGCode("3035")
				.setGeometryToCover(europeCover)
				;
		Collection<Feature> cells = grid.getCells();

		StatGridCountryUtil.assignCountries(cells, "CNTR_ID", cnts, bufferDistance, "CNTR_ID");
		//TODO assign also nuts code
		StatGridCountryUtil.filterCellsWithoutCountry(cells, "CNTR_ID");

		//save
		/*if(saveSPH) {
			logger.info("Save " + cells.size() + " cells as SHP...");
			SHPUtil.saveSHP(cells, path+resKM+"km/grid_"+resKM+"km.shp", crs);
		}*/
		logger.info("Save " + cells.size() + " cells as GPKG...");
		GeoPackageUtil.save(cells, path+"grid_"+resKM+"km.gpkg", crs);
	}

}
