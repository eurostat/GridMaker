/**
 * 
 */
package eu.europa.ec.eurostat.grid.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Some functions to load/save as GeoJSON format.
 * 
 * @author julien Gaffuri
 *
 */
public class GeoJSONUtil {

	public static SimpleFeatureCollection loadFC(String filePath) {
		try {
			InputStream input = new FileInputStream(new File(filePath));
			FeatureCollection<?,?> fc = new FeatureJSON().readFeatureCollection(input);
			input.close();
			return (SimpleFeatureCollection)fc;
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public static CoordinateReferenceSystem loadCRS(String filePath) {
		try {
			InputStream input = new FileInputStream(new File(filePath));
			CoordinateReferenceSystem crs = new FeatureJSON().readCRS(input);
			input.close();
			return crs;
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public static ArrayList<Feature>  load(String filePath) {
		SimpleFeatureCollection sfc = loadFC(filePath);
		if(sfc == null) return null;
		return SimpleFeatureUtil.get(sfc);
	}

	public static void save(Collection<? extends Feature> fs, String outFile, CoordinateReferenceSystem crs) { save(SimpleFeatureUtil.get(fs, crs), outFile); }
	public static void save(FeatureCollection<?,?> fc, String outFile) {
		try {
			OutputStream output = new FileOutputStream(new File(outFile));
			new FeatureJSON().writeFeatureCollection(fc, output);
			output.close();
		} catch (Exception e) { e.printStackTrace(); }
	}




	public static String toGeoJSON(Geometry geom){
		String out = null;
		try {
			StringWriter writer = new StringWriter();
			new GeometryJSON().write(geom, writer);
			out = writer.toString();
			writer.close();
		} catch (IOException e) { e.printStackTrace();
		} finally {
		}
		return out;
	}



	//Convert a SHP file into a geoJSON file
	public static void toGeoJSON(Collection<Feature> fs, String outPath, String outFile) { toGeoJSON(SimpleFeatureUtil.get(fs,null), outPath, outFile); }
	public static void toGeoJSON(Collection<Feature> fs, Writer writer) { toGeoJSON(SimpleFeatureUtil.get(fs,null), writer); }
	public static void toGeoJSON(SimpleFeatureCollection fc, String outPath, String outFile) {
		try {
			new File(outPath).mkdirs();
			FileWriter fw = new FileWriter(outPath + outFile);
			toGeoJSON(fc, fw);
			fw.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	public static void toGeoJSON(SimpleFeatureCollection fc, Writer writer) {
		try {
			new FeatureJSON().writeFeatureCollection(fc, writer);
		} catch (IOException e) { e.printStackTrace(); }
	}
	public static void toGeoJSON(String inSHPFilePath, String outPath, String outFile) { toGeoJSON(SHPUtil.getSimpleFeatures(inSHPFilePath), outPath, outFile); }

}
