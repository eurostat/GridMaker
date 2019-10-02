package eu.europa.ec.eurostat.grid.examples;

import java.util.ArrayList;
import java.util.Collection;

import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;
import eu.europa.ec.eurostat.grid.utils.SHPUtil;

public class CntPreparation {

	public static void main(String[] args) throws Exception {
		System.out.println("Start");

		String path = "C:\\Users\\gaffuju\\Desktop\\CNTR_100k\\";
		CoordinateReferenceSystem crs = CRS.decode("EPSG:3035");

		String[] versions = new String[] {"2004","2010","2013","2016"};

		//produce country union
		Collection<Feature> cnts = new ArrayList<>();
		for(String cntC : CountriesUtil.EuropeanCountryCodes) {
			System.out.println(cntC);
			Geometry cntGeom = null;
			for(String version : versions) {
				System.out.println(version);
				Geometry cntGeomV = null;
				try {
					cntGeomV = CountriesUtil.getEuropeanCountry(cntC, path+"CNTR_RG_100K_"+version+"_LAEA.shp").getDefaultGeometry();
					cntGeomV = cntGeomV.buffer(0);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					continue;
				}
				if(cntGeom == null)
					cntGeom = cntGeomV;
				else
					cntGeomV = cntGeom.union(cntGeomV);
			}
			Feature cnt = new Feature();
			cnt.setAttribute("CNTR_ID", cntC);
			cnt.setDefaultGeometry(cntGeom);
			cnts.add(cnt);
		}
		SHPUtil.saveSHP(cnts, path+"CNTR_RG_100K_union_LAEA.shp", crs);

		//produce europe 100k
		SHPUtil.union(path+"CNTR_RG_100K_union_LAEA.shp", path+"Europe_100K_union_LAEA.shp");

		System.out.println("End");
	}

}
