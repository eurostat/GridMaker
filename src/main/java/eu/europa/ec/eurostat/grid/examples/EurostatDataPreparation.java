package eu.europa.ec.eurostat.grid.examples;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;
import eu.europa.ec.eurostat.grid.utils.SHPUtil;

public class EurostatDataPreparation {
	static Logger logger = Logger.getLogger(EurostatDataPreparation.class.getName());

	public static void main(String[] args) throws Exception {
		logger.info("Start");

		String path = "C:\\Users\\gaffuju\\Desktop\\CNTR_100k\\";
		CoordinateReferenceSystem crs = CRS.decode("EPSG:3035");

		String[] versions = new String[] {"2004","2010","2013","2016"};


		//produce country union
		Collection<Feature> cnts = new ArrayList<>();
		for(String cntC : CountriesUtil.EuropeanCountryCodes) {
			logger.info(cntC);
			Geometry cntGeom = null;
			for(String version : versions) {
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



		logger.info("Produce europe 100k from 100k countries");
		SHPUtil.union(path+"CNTR_RG_100K_union_LAEA.shp", path+"Europe_100K_union_LAEA.shp", 0);
		//logger.info("Produce europe 100k buffer from 100k countries");
		//SHPUtil.union(path+"CNTR_RG_100K_union_LAEA.shp", path+"Europe_100K_union_LAEA_2000.shp", 2000);


		logger.info("End");
	}

}
