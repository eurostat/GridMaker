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

import eu.europa.ec.eurostat.grid.Grid;
import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
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

	//the different resolutions, in KM
	static int[] resKMs = new int[] {100,50,20,10,5,2,1};

	//see also:
	//https://www.eea.europa.eu/data-and-maps/data/eea-reference-grids-2
	//https://www.efgs.info/data/
	//https://esdac.jrc.ec.europa.eu/content/european-reference-grids

	public static void main(String[] args) throws Exception {
		logger.info("Start");

		logger.setLevel(Level.ALL);
		Grid.logger.setLevel(Level.ALL);
		GridUtil.logger.setLevel(Level.ALL);

		String outpath = "C:/Users/gaffuju/Desktop/grid/";
		String path = "C:/Users/gaffuju/Desktop/CNTR_100k/";
		CoordinateReferenceSystem crs = CRS.decode("EPSG:3035");
		int bufferDistance = 1500;

		logger.info("Get Europe cover (buffer)...");
		Geometry europeCoverBuff = SHPUtil.loadSHP(path+"Europe_100K_union_buff_"+bufferDistance+"_LAEA.shp").fs.iterator().next().getDefaultGeometry();

		logger.info("Get European countries (buffer) ...");
		ArrayList<Feature> cntsBuff = SHPUtil.loadSHP(path+"CNTR_RG_100K_union_buff_"+bufferDistance+"_LAEA.shp").fs;

		logger.info("Get land area...");
		Geometry landGeometry = SHPUtil.loadSHP(path+"Europe_100K_union_LAEA.shp").fs.iterator().next().getDefaultGeometry();

		//build pan-European grids
		for(int resKM : resKMs) {
			logger.info("Make " + resKM + "km grid...");

			//build grid
			Grid grid = new Grid()
					.setResolution(resKM*1000)
					.setEPSGCode("3035")
					.setGeometryToCover(europeCoverBuff)
					;
			Collection<Feature> cells = grid.getCells();

			//assign country codes
			GridUtil.assignRegionCode(cells, "CNTR_ID", cntsBuff, 0, "CNTR_ID");
			GridUtil.filterCellsWithoutRegion(cells, "CNTR_ID");

			//TODO assign also nuts code ?

			//assign land area
			GridUtil.assignLandProportion(cells, "LAND_PC", landGeometry, 2);

			//save as GPKG
			logger.info("Save " + cells.size() + " cells as GPKG...");
			GeoPackageUtil.save(cells, outpath+"grid_"+resKM+"km.gpkg", crs);

			//save as SHP
			if(resKM>3) {
				logger.info("Save " + cells.size() + " cells as SHP...");
				SHPUtil.saveSHP(cells, outpath + "grid_"+resKM+"km_shp" + "/grid_"+resKM+"km.shp", crs);
			}
		}



		/*
		//build country 1km grids by country
		for(String countryCode : CountriesUtil.EuropeanCountryCodes) {

			logger.info("Make 1km grid for " + countryCode + "...");

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
			SHPUtil.saveSHP(cells, outpath+"grid_1km_shp/grid_1km_"+countryCode+".shp", crs);
			//logger.info("Save " + cells.size() + " cells as GPKG...");
			//GeoPackageUtil.save(cells, outpath+"1km/grid_1km_"+countryCode+".gpkg", crs);
		}
		 */

		logger.info("End");
	}

}
