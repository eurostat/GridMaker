package eu.europa.ec.eurostat.grid.examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;
import eu.europa.ec.eurostat.grid.utils.SHPUtil;

public class EurostatDataPreparation {
	static Logger logger = Logger.getLogger(EurostatDataPreparation.class.getName());

	//use: -Xms2G -Xmx6G
	public static void main(String[] args) throws Exception {
		logger.info("Start");

		String path = "C:\\Users\\gaffuju\\Desktop\\CNTR_100k\\";

		//logger.info("Produce country geometry as the union of different versions");
		//produceCountriesUnionVersions(path);


		//logger.info("Produce Europe 100k as union of countries");
		//SHPUtil.union(path+"CNTR_RG_100K_union_LAEA.shp", path+"Europe_100K_union_LAEA.shp", 0);

		//buffering
		int bufferDistance = 2000; //TODO change to 1500 only?
		logger.info("Produce buffers (" + bufferDistance + ") of countries");
		buffer(path+"CNTR_RG_100K_union_LAEA.shp", path+"CNTR_RG_100K_union_buff_" + bufferDistance + "_LAEA.shp", bufferDistance, 4, BufferParameters.CAP_ROUND);
		logger.info("Produce Europe (" + bufferDistance + ") buffer");
		buffer(path+"Europe_100K_union_LAEA.shp", path+"Europe_100K_union_buff_" + bufferDistance + "_LAEA.shp", bufferDistance, 4, BufferParameters.CAP_ROUND);

		//TODO remove country holes ?

		logger.info("End");
	}



	//compute the buffers of features of a SHP file
	public static void buffer(String inFile, String outFile, double bufferDistance, int quadrantSegments, int endCapStyle){

		//load data
		ArrayList<Feature> fs = SHPUtil.loadSHP(inFile).fs;

		for(Feature f : fs) {
			System.out.println(f.getAttribute("CNTR_ID"));
			Geometry geom = (Geometry) f.getDefaultGeometry();
			Collection<Geometry> geoms = getGeometries(geom);
			System.out.println(geoms.size() + " components");
			System.out.println("Compute buffers");
			Collection<Geometry> buffs = new ArrayList<>();
			for(Geometry g : geoms)
				buffs.add(g.buffer(bufferDistance, quadrantSegments, endCapStyle));
			System.out.println("Compute union of buffers");
			Geometry buff = new CascadedPolygonUnion(buffs ).union();
			if(buff instanceof Polygon) buff = buff.getFactory().createMultiPolygon(new Polygon[] {(Polygon)buff});
			f.setDefaultGeometry(buff);
		}

		System.out.println("Save");
		SHPUtil.saveSHP(fs, outFile, SHPUtil.getCRS(inFile));
	}

	//return list of geometries that are not GeometryCollection
	public static Collection<Geometry> getGeometries(Geometry geom){
		Collection<Geometry> out = new HashSet<Geometry>();
		int nb = geom.getNumGeometries();
		if(nb == 0)
			return out;
		if(nb == 1)
			out.add(geom.getGeometryN(0));
		else
			for(int i=0; i<nb; i++)
				out.addAll( getGeometries(geom.getGeometryN(i)) );
		return out;
	}



	//produce country geometry as the union of different versions
	private static void produceCountriesUnionVersions(String path) throws Exception {
		CoordinateReferenceSystem crs = CRS.decode("EPSG:3035");

		//NB: CascadedPolygonUnion returned noding exception - maybe it could be better tested...
		Collection<Feature> cnts = new ArrayList<>();
		for(String cntC : CountriesUtil.EuropeanCountryCodes) {
			logger.info(cntC);
			Geometry cntGeom = null;
			for(String version : new String[] {"2004","2010","2013","2016"}) {
				logger.info(version);

				Geometry cntGeomV = null;
				try {
					cntGeomV = CountriesUtil.getEuropeanCountry(cntC, path+"CNTR_RG_100K_"+version+"_LAEA.shp").getDefaultGeometry();
					cntGeomV = cntGeomV.buffer(0);
				} catch (Exception e) {
					logger.info(e.getMessage());
					continue;
				}
				if(cntGeom == null)
					cntGeom = cntGeomV;
				else
					try {
						cntGeom = cntGeom.union(cntGeomV);
					} catch (Exception e) {
						logger.info("Retry...");
						cntGeom = cntGeom.union(cntGeomV.buffer(0.01));
					}
			}
			Feature cnt = new Feature();
			cnt.setAttribute("CNTR_ID", cntC);
			cnt.setDefaultGeometry(cntGeom);
			cnts.add(cnt);
		}
		SHPUtil.saveSHP(cnts, path+"CNTR_RG_100K_union_LAEA.shp", crs);
	}

}
