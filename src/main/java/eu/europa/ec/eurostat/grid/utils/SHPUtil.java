package eu.europa.ec.eurostat.grid.utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Some functions to load/save as SHP format.
 * 
 * @author julien Gaffuri
 *
 */
public class SHPUtil {
	private final static Logger LOGGER = Logger.getLogger(SHPUtil.class);

	//get basic info on shp file

	public static SimpleFeatureType getSchema(String shpFilePath){
		try {
			File file = new File(shpFilePath);
			if(!file.exists()) throw new IOException("File "+shpFilePath+" does not exist.");
			return FileDataStoreFinder.getDataStore(file).getSchema();
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}
	public static String[] getAttributeNames(String shpFilePath){
		return SimpleFeatureUtil.getAttributeNames(getSchema(shpFilePath));
	}
	public static CoordinateReferenceSystem getCRS(String shpFilePath){
		return getSchema(shpFilePath).getCoordinateReferenceSystem();
	}
	public static Envelope getBounds(String shpFilePath) {
		return getSimpleFeatures(shpFilePath).getBounds();
	}


	//load

	public static SimpleFeatureCollection getSimpleFeatures(String shpFilePath){ return getSimpleFeatures(shpFilePath, null); }
	public static SimpleFeatureCollection getSimpleFeatures(String shpFilePath, Filter f){
		try {
			File file = new File(shpFilePath);
			if(!file.exists()) throw new IOException("File "+shpFilePath+" does not exist.");
			FileDataStore store = FileDataStoreFinder.getDataStore(file);
			SimpleFeatureCollection a = store.getFeatureSource().getFeatures(f);
			//DefaultFeatureCollection sfs = DataUtilities.collection(a);
			store.dispose();
			return a;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class SHPData{
		public SimpleFeatureType ft;
		public ArrayList<Feature> fs;
		public ReferencedEnvelope env;
		public SHPData(SimpleFeatureType ft, ArrayList<Feature> fs, ReferencedEnvelope env){
			this.ft=ft; this.fs=fs; this.env=env;
		}
	}

	public static SHPData loadSHP(String shpFilePath) { return loadSHP(shpFilePath, null); }
	public static SHPData loadSHP(String shpFilePath, Filter f) {
		SimpleFeatureCollection sfs = getSimpleFeatures(shpFilePath, f);
		SHPData sd = new SHPData(sfs.getSchema(), SimpleFeatureUtil.get(sfs), sfs.getBounds());
		return sd;
	}




	//save

	//public static void saveSHP(Collection<Feature> fs, String outFile) { saveSHP(fs, outFile, null); }
	public static void saveSHP(Collection<? extends Feature> fs, String outFile, CoordinateReferenceSystem crs) { saveSHP(fs,outFile,crs,null); }
	public static void saveSHP(Collection<? extends Feature> fs, String outFile, CoordinateReferenceSystem crs, List<String> atts) { saveSHP(SimpleFeatureUtil.get(fs, crs, atts), outFile); }
	public static void saveSHP(SimpleFeatureCollection sfs, String outFile) {
		try {
			if(sfs.size() == 0){
				//file.createNewFile();
				LOGGER.warn("Could not save file "+outFile+" - collection of features is empty");
				return;
			}

			//create output file
			File file = getFile(outFile, true, true);

			//create feature store
			HashMap<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", file.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);
			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);

			ds.createSchema(sfs.getSchema());
			SimpleFeatureStore fst = (SimpleFeatureStore)ds.getFeatureSource(ds.getTypeNames()[0]);

			//creation transaction
			Transaction tr = new DefaultTransaction("create");
			fst.setTransaction(tr);
			try {
				fst.addFeatures(sfs);
				tr.commit();
			} catch (Exception e) {
				e.printStackTrace();
				tr.rollback();
			} finally {
				tr.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File getFile(String filePath, boolean createFolders, boolean eraseOnExist){
		if(createFolders) createFolders(filePath);
		File file = new File(filePath);
		if(eraseOnExist && file.exists()) file.delete();
		return file;
	}

	public static void createFolders(String filePath){
		File parent = new File(filePath).getParentFile();
		if (!parent.exists() && !parent.mkdirs())
			throw new IllegalStateException("Couldn't create dir: " + parent);
	}



	//save the union of a shapefile into another one (applying a buffer is required)
	public static void union(String inFile, String outFile, double bufferDistance){
		try {
			//load input shp
			ArrayList<Feature> fs = loadSHP(inFile).fs;

			//build union
			ArrayList<Geometry> geoms = new ArrayList<Geometry>();
			for( Feature f : fs ) {
				Geometry geom = f.getDefaultGeometry();
				if((geom instanceof Polygon || geom instanceof MultiPolygon) && !geom.isValid()) {
					geom = geom.buffer(0);
				}
				geoms.add(geom);
			}
			Geometry union = new CascadedPolygonUnion(geoms).union();

			if(bufferDistance != 0)
				union = union.buffer(bufferDistance);

			//build feature
			SimpleFeatureBuilder fb = new SimpleFeatureBuilder(DataUtilities.createType("ep", "the_geom:"+union.getGeometryType()));
			fb.add(union);
			SimpleFeature sf = fb.buildFeature(null);

			//save shp
			DefaultFeatureCollection outfc = new DefaultFeatureCollection(null,null);
			outfc.add(sf);
			saveSHP(outfc, outFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void buffer(String inFile, String outFile, double bufferDistance){
		try {
			SimpleFeatureCollection sfs = getSimpleFeatures(inFile);
			SimpleFeatureIterator iterator = sfs.features();
			try {
				while( iterator.hasNext()  ){
					SimpleFeature f = iterator.next();
					f.setDefaultGeometry( ((Geometry)f.getDefaultGeometry()).buffer(bufferDistance) );
				}
			}
			finally {
				iterator.close();
			}

			saveSHP(sfs, outFile);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
