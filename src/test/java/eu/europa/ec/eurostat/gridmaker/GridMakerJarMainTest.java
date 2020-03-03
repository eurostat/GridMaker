package eu.europa.ec.eurostat.gridmaker;

import junit.framework.TestCase;

public class GridMakerJarMainTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(GridMakerJarMainTest.class);
	}

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

	public void testParams() throws Exception {
		for(String format : new String[] {"shp","gpkg","geojson"})
			GridMakerJarMain.main(new String[] {"-o", "target/test/outformat"+"."+format});
	}

	//TODO test gt
	//TODO test tol
	//TODO test inputs + formats
	//TODO test output formats

}
