package eu.europa.ec.eurostat.grid.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Efficient union of polygons.
 * 
 * @author julien Perret (from GeOxygene project http://sourceforge.net/projects/oxygene-project/)
 *
 */
public class Union {
	private final static Logger LOGGER = Logger.getLogger(Union.class.getName());

	public static Geometry getPolygonUnion(Collection<Geometry> geoms) {
		return getPolygon( union_(geoms) );
	}

	private static <T extends Geometry> ArrayList<Geometry> union_(Collection<T> geoms) {
		ArrayList<Geometry> geoms_ = new ArrayList<Geometry>();
		geoms_.addAll(geoms);

		final int cellSize = 1 + (int)Math.sqrt(geoms_.size());

		Comparator<Geometry> comparator =  new Comparator<Geometry>(){
			public int compare(Geometry geom1, Geometry geom2) {
				if (geom1==null || geom2==null) return 0;
				Envelope env1 = geom1.getEnvelopeInternal();
				Envelope env2 = geom2.getEnvelopeInternal();
				double i1 = env1.getMinX() / cellSize + cellSize*( (int)env1.getMinY() / cellSize );
				double i2 = env2.getMinX() / cellSize + cellSize*( (int)env2.getMinY() / cellSize );
				return i1>=i2? 1 : i1<i2? -1 : 0;
			}
		};

		int i = 1;
		int nb = 1 + (int)( Math.log(geoms_.size()) / Math.log(4) );
		TreeSet<Geometry> treeSet;
		while (geoms_.size() > 1) {
			i++;
			if(LOGGER.isTraceEnabled()) LOGGER.trace("Union (" + (i-1) + "/" + nb + ")");
			//System.out.println( "Union (" + (i-1) + "/" + nb + ")" );
			treeSet = new TreeSet<Geometry>(comparator);
			treeSet.addAll(geoms_);
			geoms_ = union(treeSet, 4);
		}
		return geoms_;
	}

	private static ArrayList<Geometry> union(TreeSet<Geometry> treeSet, int groupSize) {
		ArrayList<Geometry> unions = new ArrayList<Geometry>();
		Geometry union = null;
		int i=0;
		for (Geometry geom : treeSet) {
			if ((union==null)||(i%groupSize==0)) union = geom;
			else {
				union = union.union(geom);
				if (groupSize-i%groupSize==1) unions.add(union);
			}
			i++;
			if(LOGGER.isTraceEnabled()) LOGGER.trace(" " + i + " - " + treeSet.size() + " geometries");
		}
		if (groupSize-i%groupSize!=0) unions.add(union);
		return unions;
	}

	private static Geometry getPolygon(ArrayList<Geometry> geoms_) {
		List<Polygon> polys = new ArrayList<Polygon>();
		for (Geometry geom : geoms_) {
			if (geom instanceof Polygon) polys.add((Polygon) geom);
			else if (geom instanceof MultiPolygon) {
				MultiPolygon mp = (MultiPolygon) geom;
				for (int k=0; k<mp.getNumGeometries(); k++)
					polys.add((Polygon)mp.getGeometryN(k));
			} else LOGGER.error("Error in polygon union: geometry type not supported: " + geom.getGeometryType());
		}
		if (polys.size()==1) return polys.get(0);
		if (geoms_.isEmpty()) return new GeometryFactory().createGeometryCollection(new Geometry[0]);
		return geoms_.iterator().next().getFactory().createMultiPolygon(polys.toArray(new Polygon[0]));

	}


}
