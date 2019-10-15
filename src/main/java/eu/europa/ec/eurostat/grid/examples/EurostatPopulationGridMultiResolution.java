/**
 * 
 */
package eu.europa.ec.eurostat.grid.examples;

import org.apache.log4j.Logger;

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

		//load 1km population

		for(int resKM : new int[] {100,50,20,10,5}) {
			//TODO
			//go through 1km population data
			//get higher resolution grid cell it belong to
			//increment population value

			//save
		}

		logger.info("End");
	}

}
