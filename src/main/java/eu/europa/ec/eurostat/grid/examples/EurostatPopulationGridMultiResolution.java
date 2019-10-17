/**
 * 
 */
package eu.europa.ec.eurostat.grid.examples;

import org.apache.log4j.Logger;

import eu.europa.ec.eurostat.java4eurostat.base.StatsHypercube;
import eu.europa.ec.eurostat.java4eurostat.io.CSV;

/**
 * 
 * Derive EU population figures from 1km grid to other larger resolutions.
 * 
 * @author julien Gaffuri
 *
 */
public class EurostatPopulationGridMultiResolution {
	static Logger logger = Logger.getLogger(EurostatPopulationGridMultiResolution.class.getName());

	static String basePath = "C:/Users/gaffuju/Desktop/";

	public static void main(String[] args) {
		logger.info("Start");

		logger.info("End");
	}

	private void produceMultiResolutionPopGrids() {

		for(int year : new int[] { 2006, 2011 }) {
			logger.info(year);

			//load 1km data
			StatsHypercube popData = CSV.load(basePath+"pop_grid_1km/"+year+".csv", "TOT_P");

			//remove unnecessary dimensions
			popData.delete("YEAR");
			popData.delete("CNTR_CODE");
			popData.delete("METHD_CL");
			popData.delete("DATA_SRC");
			if(year==2011) popData.delete("TOT_P_CON_DT");

			for(int resKM : EurostatGridsProduction.resKMs) {

				//output data
				StatsHypercube sh = new StatsHypercube("GRD_ID");

				//TODO
				//go through 1km population data
				//get higher resolution grid cell it belong to
				//increment population value

				//save
				CSV.save(sh, "TOT_P", basePath+"pop_grid/", "pop_grid_"+year+"_"+resKM+"km.csv");
			}
		}

	}



	private void reFormatGeostatFiles() {

		for(int year : new int[] { 2006, 2011 }) {
			logger.info(year);

			//load 1km data
			StatsHypercube popData = CSV.load(basePath+"pop_grid_1km/"+year+".csv", "TOT_P");

			//GRD_ID; TOT_P; YEAR; CNTR_CODE; METHD_CL; DATA_SRC
			//grid cell example: 1kmN2689E4341

			//TODO reformat grid cell
			CSV.save(popData, "TOT_P", basePath+"pop_grid/", "pop_grid_"+year+"_1km.csv");

		}
	}

}
