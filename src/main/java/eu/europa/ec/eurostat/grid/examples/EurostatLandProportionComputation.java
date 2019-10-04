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
		for(int resKM : new int[] {100,50,20,10,5}) {
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
