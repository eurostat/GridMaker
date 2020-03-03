package eu.europa.ec.eurostat.gridmaker;

import junit.framework.TestCase;

public class GridMakerJarMainTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(GridMakerJarMainTest.class);
	}

	public void testDefault() throws Exception {
		GridMakerJarMain.main(new String[] {"-o", "target/test/out.gpkg"});
	}

	public void testParams() throws Exception {
		//TODO test res
		//TODO test epsg
		//TODO test gt
		//TODO test gt
		//TODO test tol
		//TODO test inputs + formats
		//TODO test output formats
		GridMakerJarMain.main(new String[] {"-res", "500000", "-epsg", "2154", "-gt", "CENTER_POINT", "-o", "target/test/params.shp"});
	}

}
