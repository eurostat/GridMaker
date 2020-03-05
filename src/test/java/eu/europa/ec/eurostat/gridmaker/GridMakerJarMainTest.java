package eu.europa.ec.eurostat.gridmaker;

import junit.framework.TestCase;

/**
 * Tests of different cases, for each possible parameter.
 * 
 * @author julien Gaffuri
 *
 */
public class GridMakerJarMainTest extends TestCase {

	/*
	public static void main(String[] args) {
		junit.textui.TestRunner.run(GridMakerJarMainTest.class);
	}*/

	public void testDefault() throws Exception {
		GridMakerJarMain.main(new String[] {"-o", "target/test/out.gpkg"});
	}

	public void testResolution() throws Exception {
		for(String res : new String[] {"100000","200000","500000","1000000"})
			GridMakerJarMain.main(new String[] {"-res", res, "-o", "target/test/res_"+res+".gpkg"});
	}

	public void testEPSG() throws Exception {
		for(String epsg : new String[] {"3035","2154", "3857"})
			GridMakerJarMain.main(new String[] {"-epsg", epsg, "-o", "target/test/epsg_"+epsg+".gpkg"});
	}

	public void testOutput() throws Exception {
		for(String format : new String[] {"shp","gpkg","geojson"})
			GridMakerJarMain.main(new String[] {"-o", "target/test/outformat_"+format+"."+format});
	}

	public void testGeomTypes() throws Exception {
		for(String gt : new String[] {"SURF","CPT"})
			GridMakerJarMain.main(new String[] {"-gt", gt, "-o", "target/test/gt_"+gt+".gpkg"});
	}

	public void testTol() throws Exception {
		for(String tol : new String[] {"0","10000","100000","500000","1000000"})
			GridMakerJarMain.main(new String[] {"-tol", tol, "-o", "target/test/tol_"+tol+".gpkg"});
	}

	public void testInputFormatResolution() throws Exception {
		for(String res : new String[] {"100000","200000","500000","1000000"})
			for(String format : new String[] {"shp","gpkg","geojson"})
				GridMakerJarMain.main(new String[] {"-i", "src/test/resources/test_grid_area."+format,"-res", res,"-o", "target/test/in_"+format+"_"+res+"."+format});
	}

}
