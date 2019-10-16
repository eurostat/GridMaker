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

		//****: GRD_ID;TOT_P;  YEAR;CNTR_CODE;METHD_CL;DATA_SRC
		//grid cell example: 1kmN2689E4341

		for(int year : new int[] { 2006, 2011 }) {
			logger.info(year);

			//load 1km data
			StatsHypercube popData = CSV.load(basePath+"pop_grid_1km/"+year+".csv", "TOT_P");
			popData.printInfo(false);

			for(int resKM : EurostatGridsProduction.resKMs) {


				//TODO
				//go through 1km population data
				//get higher resolution grid cell it belong to
				//increment population value

				//save
			}

		}


		logger.info("End");
	}

}
