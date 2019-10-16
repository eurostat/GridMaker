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

	public static void main(String[] args) {
		logger.info("Start");

		String basePath = "C:/Users/gaffuju/Desktop/";

		//GRD_ID; TOT_P; YEAR; CNTR_CODE; METHD_CL; DATA_SRC
		//grid cell example: 1kmN2689E4341

		for(int year : new int[] { 2006, 2011 }) {
			logger.info(year);

			//load 1km data
			StatsHypercube popData = CSV.load(basePath+"pop_grid_1km/"+year+".csv", "TOT_P");
			popData.printInfo(false);
			//TODO remove unnecessary dimensions
			popData.delete("YEAR");
			popData.delete("CNTR_CODE");
			popData.delete("METHD_CL");
			popData.delete("DATA_SRC");
			popData.printInfo(false);

			//TODO reformat grid cell
			//CSV.save(popData, "TOT_P", basePath+"pop_grid/", "pop_grid_"+year+"_1km.csv");

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
		logger.info("End");
	}

}
