/**
 * 
 */
package eu.europa.ec.eurostat.grid.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.geotools.filter.text.cql2.CQL;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;

/**
 * Some base functions to get some information on specific countries.
 * 
 * @author julien Gaffuri
 *
 */
public class CountriesUtil {

	//public static final String[] EuropeanCountryCodes = new String[] {"BE","BG","CZ","DK","DE","EE","IE","EL","ES","FR","HR","IT","CY","LV","LT","LU","HU","MT","NL","AT","PL","PT","RO","SI","SK","FI","SE","UK","IS","LI","NO","CH","ME","MK","AL","RS","TR"};
	public static final String[] EuropeanCountryCodes = new String[] {"AD","AL","AT","BA","BE","BG","BY","CH","CS","CY","CZ","DE","DK","EE","EL","ES","FI","FO","FR","GG","GI","HR","HU","IE","IM","IS","IT","JE","LI","LT","LU","LV","MC","MD","MK","MT","NL","NO","PL","PT","RO","SE","SI","SK","SM","TR","UA","UK","VA"};


	public static ArrayList<Feature> getEuropeanCountries(String filePath) {
		return SHPUtil.loadSHP(filePath).fs;
	}
	public static ArrayList<Feature> getEuropeanCountries(boolean withOST) {
		return getEuropeanCountries("./src/main/resources/CNTR/CNTR_RG_01M_2016"+(withOST?"":"_no_ost")+".shp");
	}

	public static Feature getEuropeanCountry(String countryCode, String filePath) {
		try {
			ArrayList<Feature> fs = SHPUtil.loadSHP(filePath, CQL.toFilter("CNTR_ID = '"+countryCode+"'")).fs;
			if(fs.size() != 1) throw new Exception("Problem finding country with code: "+countryCode+". nb found="+fs.size());
			return fs.iterator().next();
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}
	public static Feature getEuropeanCountry(String countryCode, boolean withOST) {
		return getEuropeanCountry(countryCode, "./src/main/resources/CNTR/CNTR_RG_01M_2016"+(withOST?"":"_no_ost")+".shp");
	}




	public static Geometry getEurope(boolean withOST) {
		return SHPUtil.loadSHP("./src/main/resources/CNTR/Europe_RG_01M_2016"+(withOST?"":"_no_ost")+".shp").fs.iterator().next().getDefaultGeometry();
	}

	//build Europe geometry as a union of the country geometries
	public static void makeEuropeGeometry(boolean withOST) throws Exception {
		//generate europe geometry as union of country geometries
		Collection<Geometry> polys = new ArrayList<>();
		for(Feature f : CountriesUtil.getEuropeanCountries(withOST))
			polys.add( f.getDefaultGeometry().buffer(0) );
		Geometry mask = CascadedPolygonUnion.union(polys);

		Feature f = new Feature(); f.setDefaultGeometry(mask);
		ArrayList<Feature> fs = new ArrayList<Feature>(); fs.add(f);
		SHPUtil.saveSHP(fs, "./src/main/resources/CNTR/Europe_RG_01M_2016"+(withOST?"":"_no_ost")+".shp", CRS.decode("EPSG:3035"));
	}

	/*public static void main(String[] args) throws Exception {
		makeEuropeGeometry(false);
		makeEuropeGeometry(true);
	}*/

}
