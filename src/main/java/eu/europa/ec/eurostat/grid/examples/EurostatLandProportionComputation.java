/**
 * 
 */
package eu.europa.ec.eurostat.grid.examples;

import org.apache.log4j.Logger;

/**
 * @author julien Gaffuri
 *
 */
public class EurostatLandProportionComputation {
	static Logger logger = Logger.getLogger(EurostatLandProportionComputation.class.getName());

	public static void main(String[] args) {
		logger.info("Start");

		//load country dataset (europe?)
		//TODO
		for(int resKM : EurostatGridsProduction.resKMs) {
			//load grid dataset
			//TODO
			//compute intersection
			//TODO
			//compute area + proportion
			//TODO
			//store data
			//TODO
		}

		logger.info("End");
	}

}
