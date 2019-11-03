/**
 * 
 */
package eu.europa.ec.eurostat.grid.examples;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
import eu.europa.ec.eurostat.jgiscotools.util.Util;

/**
 * A number of functions on grids.
 * 
 * @author julien Gaffuri
 *
 */
public class GridUtil {
	static Logger logger = Logger.getLogger(GridUtil.class.getName());


	/**
	 * Assign region codes to grid cells.
	 * If a grid cell intersects or is nearby the geometry of a region, then an attribute of the cell is assigned with this region code.
	 * For cells that are to be assigned to several regions, several region codes are assigned.
	 * 
	 * @param cells
	 * @param cellRegionAttribute
	 * @param regions
	 * @param toleranceDistance
	 * @param regionCodeAttribute
	 */
	public static void assignRegionCode(Collection<Feature> cells, String cellRegionAttribute, Collection<Feature> regions, double toleranceDistance, String regionCodeAttribute) {
		if(logger.isDebugEnabled()) logger.debug("Assign regions...");

		//initialise cell region attribute
		for(Feature cell : cells)
			cell.setAttribute(cellRegionAttribute, "");

		//index cells
		STRtree index = new STRtree();
		for(Feature cell : cells)
			index.insert(cell.getDefaultGeometry().getEnvelopeInternal(), cell);

		for(Feature reg : regions) {
			//get region cover and code
			Geometry regCover = reg.getDefaultGeometry();
			if(toleranceDistance != 0 ) regCover = regCover.buffer(toleranceDistance);
			String regCode = reg.getAttribute(regionCodeAttribute).toString();

			//get region envelope, expanded by toleranceDistance
			Envelope regCoverEnv = regCover.getEnvelopeInternal();

			//get grid cells around region envelope
			for(Object cell_ : index.query(regCoverEnv)) {
				Feature cell = (Feature)cell_;
				Geometry cellGeom = cell.getDefaultGeometry();

				if( ! regCoverEnv.intersects(cellGeom.getEnvelopeInternal()) ) continue;
				if( ! regCover.intersects(cellGeom) ) continue;

				String att = cell.getAttribute(cellRegionAttribute).toString();
				if("".equals(att))
					cell.setAttribute(cellRegionAttribute, regCode);
				else
					cell.setAttribute(cellRegionAttribute, att+"-"+regCode);
			}
		}

	}


	/**
	 * Remove cells which are not assigned to any region,
	 * that is the ones with attribute 'cellRegionAttribute' null or set to "".
	 * 
	 * @param cells
	 * @param cellRegionAttribute
	 */
	public static void filterCellsWithoutRegion(Collection<Feature> cells, String cellRegionAttribute) {
		if(logger.isDebugEnabled()) logger.debug("Filtering " + cells.size() + " cells...");
		Collection<Feature> cellsToRemove = new ArrayList<Feature>();
		for(Feature cell : cells) {
			Object cellReg = cell.getAttribute(cellRegionAttribute);
			if(cellReg==null || "".equals(cellReg.toString())) cellsToRemove.add(cell);
		}
		cells.removeAll(cellsToRemove);
		if(logger.isDebugEnabled()) logger.debug(cellsToRemove.size() + " cells to remove. " + cells.size() + " cells left");
	}


	/**
	 * Compute for each cell the proportion of its area which is land area.
	 * The value is stored as a new attribute for each cell. This value is a percentage.
	 * 
	 * @param cells
	 * @param cellLandPropAttribute
	 * @param landGeometry
	 * @param decimalNB The number of decimal places to keep for the percentage
	 */
	public static void assignLandProportion(Collection<Feature> cells, String cellLandPropAttribute, Geometry landGeometry, int decimalNB) {
		if(logger.isDebugEnabled()) logger.debug("Assign land proportion...");

		//compute cell area once
		double cellArea = cells.iterator().next().getDefaultGeometry().getArea();

		for(Feature cell : cells) {
			Geometry inter = cell.getDefaultGeometry().intersection(landGeometry); //TODO test if other way around is quicker
			double prop = 100.0 * inter.getArea() / cellArea;
			prop = Util.round(prop, decimalNB);
			cell.setAttribute(cellLandPropAttribute, prop);
		}

	}

}
