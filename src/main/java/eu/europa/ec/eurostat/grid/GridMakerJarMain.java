/**
 * 
 */
package eu.europa.ec.eurostat.grid;

import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;

import eu.europa.ec.eurostat.grid.StatGrid.GridCellGeometryType;
import eu.europa.ec.eurostat.grid.utils.CountriesUtil;
import eu.europa.ec.eurostat.grid.utils.Feature;
import eu.europa.ec.eurostat.grid.utils.SHPUtil;
import eu.europa.ec.eurostat.grid.utils.SHPUtil.SHPData;

/**
 * 
 * The class for the executable program.
 * 
 * @author julien Gaffuri
 *
 */
public class GridMakerJarMain {

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption(Option.builder("res").longOpt("resolution").desc("Optional. The grid resolution (pixel size). Note that the unit of measure is expected to be the same as the one of the coordinate reference system. Default: '100 000'.")
				.hasArg().argName("value").build());
		options.addOption(Option.builder("epsg").longOpt("epsgCode").desc("Optional. The EPSG code of the grid coordinate reference system. Default: '3035', corresponding to ETRS89-LAEA coordinate reference system.")
				.hasArg().argName("code").build());

		options.addOption(Option.builder("cnt").longOpt("countryCode").desc("The 2 letters code of the country to build the grid accross.")
				.hasArg().argName("code").build());
		//TODO envelope
		options.addOption(Option.builder("i").longOpt("inputFile").desc("Input file (SHP format) containing the geometry of the region to build the grid accross.")
				.hasArg().argName("file path").build());

		options.addOption(Option.builder("tol").longOpt("toleranceDistance").desc("Optional. A tolerance distance to keep the cells that are not too far from the specified region. Default: '0'.")
				.hasArg().argName("value").build());
		options.addOption(Option.builder("gt").longOpt("gridCellGeometryType").desc("Optional. The type of grid cell geometry: The surface representation (a square) or its center point. Default: 'SURFACE'.")
				.hasArg().argName("SURFACE or CENTER_POINT").build());
		options.addOption(Option.builder("o").longOpt("outputFile").desc("Optional. Output file (SHP format). Default: 'out.shp'.")
				.hasArg().argName("file path").build());
		options.addOption(Option.builder("h").desc("Show this help message").build());

		CommandLine cmd = null;
		try { cmd = new DefaultParser().parse( options, args); } catch (ParseException e) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			return;
		}

		//help statement
		if(cmd.hasOption("h")) {
			new HelpFormatter().printHelp("java -jar searoute.jar", options);
			return;
		}

		System.out.println("Read parameters...");
		String param;

		StatGrid sg = new StatGrid();

		//res
		param = cmd.getOptionValue("res");
		try { if(param != null) sg.setResolution(Double.parseDouble(param));
		} catch (Exception e) { System.err.println("Failed reading parameter 'res'. The default value will be used."); }

		//epsg
		param = cmd.getOptionValue("epsg");
		try { if(param != null) sg.setEPSGCode(param);
		} catch (Exception e) { System.err.println("Failed reading parameter 'epsg'. The default value will be used."); }

		//cnt
		param = cmd.getOptionValue("cnt");
		try { if(param != null) {
			Feature cnt = CountriesUtil.getEuropeanCountry(param, true);
			Geometry geomToCover = cnt.getDefaultGeometry();
			sg.setGeometryToCover(geomToCover);
		}
		} catch (Exception e) { System.err.println("Failed finding country with parameter 'cnt': " + param); }

		//i
		param = cmd.getOptionValue("i");
		try { if(param != null) {
			SHPData sh = SHPUtil.loadSHP(param);
			if(sh.fs.size() == 0)
				System.err.println("Input file is empty: " + param);
			else {
				if(sh.fs.size() > 1)
					System.err.println("Input file contains more than one geometry (nb="+sh.fs.size()+"): " + param);
				Geometry geomToCover = sh.fs.iterator().next().getDefaultGeometry();
				sg.setGeometryToCover(geomToCover);
			}
		}
		} catch (Exception e) { System.err.println("Failed reading input geometry from file: " + param); }

		//tol
		param = cmd.getOptionValue("tol");
		try { if(param != null) sg.setToleranceDistance(Double.parseDouble(param));
		} catch (Exception e) { System.err.println("Failed reading parameter 'tol'. The default value will be used."); }

		//gt
		param = cmd.getOptionValue("gt");
		try { if(param != null) {
			if(param == GridCellGeometryType.SURFACE.toString()) sg.setGridCellGeometryType(GridCellGeometryType.SURFACE);
			if(param == GridCellGeometryType.CENTER_POINT.toString()) sg.setGridCellGeometryType(GridCellGeometryType.CENTER_POINT);
			else throw new Exception();
		}
		} catch (Exception e) { System.err.println("Failed reading parameter 'gt'. The default value will be used."); }

		//output file
		String outFile = cmd.getOptionValue("o");
		if(outFile == null) outFile = Paths.get("").toAbsolutePath().toString() + "/out.json";


		System.out.println("Build grid...");
		Collection<Feature> cells = sg.getCells();
		System.out.println(cells.size() + " grid cells built.");


		System.out.println("Save as " + outFile + "...");
		try {
			SHPUtil.saveSHP(cells, outFile, CRS.decode("EPSG:"+sg.getEPSGCode()));
		} catch (FactoryException e) { e.printStackTrace(); }

	}

}
