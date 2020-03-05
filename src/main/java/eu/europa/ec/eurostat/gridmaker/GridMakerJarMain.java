/**
 * 
 */
package eu.europa.ec.eurostat.gridmaker;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
import eu.europa.ec.eurostat.jgiscotools.feature.SimpleFeatureUtil;
import eu.europa.ec.eurostat.jgiscotools.grid.Grid;
import eu.europa.ec.eurostat.jgiscotools.grid.GridCell.GridCellGeometryType;
import eu.europa.ec.eurostat.jgiscotools.io.GeoJSONUtil;
import eu.europa.ec.eurostat.jgiscotools.io.GeoPackageUtil;
import eu.europa.ec.eurostat.jgiscotools.io.SHPUtil;

/**
 * 
 * The class for the executable program.
 * 
 * @author julien Gaffuri
 *
 */
public class GridMakerJarMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//define options
		Options options = new Options();
		options.addOption(Option.builder("res").longOpt("resolution").desc("Optional. The grid resolution (pixel size). Note that the unit of measure is expected to be the same as the one of the coordinate reference system. Default: '100 000'.")
				.hasArg().argName("value").build());
		options.addOption(Option.builder("epsg").longOpt("epsgCode").desc("Optional. The EPSG code of the grid coordinate reference system. Default: '3035', corresponding to ETRS89-LAEA coordinate reference system.")
				.hasArg().argName("code").build());
		options.addOption(Option.builder("i").longOpt("inputFile").desc("Input file containing the geometry of the region to build the grid accross. Supported format: GeoJSON, SHP, GeoPackage.")
				.hasArg().argName("file path").build());
		options.addOption(Option.builder("tol").longOpt("toleranceDistance").desc("Optional. A tolerance distance to keep the cells that are not too far from the specified region. Default: '0'.")
				.hasArg().argName("value").build());
		options.addOption(Option.builder("gt").longOpt("gridCellGeometryType").desc("Optional. The type of grid cell geometry: The surface representation (a square) or its center point. Default: 'SURFACE'.")
				.hasArg().argName("SURFACE or CENTER_POINT").build());
		options.addOption(Option.builder("o").longOpt("outputFile").desc("Optional. Output file. The supported formats are GeoJSON (*.geojson extension), SHP (*.shp extension) and GeoPackage (*.gpkg extension). Default: 'out.gpkg'.")
				.hasArg().argName("file path").build());
		//TODO: add parameters: envelope dimensions

		options.addOption(Option.builder("h").desc("Show this help message").build());

		//read options
		CommandLine cmd = null;
		try { cmd = new DefaultParser().parse( options, args); } catch (ParseException e) {
			System.err.println("Error when reading parameters.");
			System.err.println(" Reason: " + e.getMessage() );
			return;
		}

		//help statement
		if(cmd.hasOption("h")) {
			new HelpFormatter().printHelp("java -jar searoute.jar", options);
			return;
		}

		System.out.println("Read parameters...");
		String param;

		//create grid object
		Grid sg = new Grid();

		//res
		param = cmd.getOptionValue("res");
		if(param != null)
			try {
				//TODO allow double values, for geographical grids for example?
				sg.setResolution(Integer.parseInt(param));
			} catch (Exception e) { System.err.println("Failed reading parameter 'res'. The default value will be used."); }

		//epsg
		param = cmd.getOptionValue("epsg");
		if(param != null)
			try {
				sg.setEPSGCode(param);
			} catch (Exception e) {
				System.err.println("Failed reading parameter 'epsg'. The default value will be used.");
				System.err.println(param);
			}

		//i
		param = cmd.getOptionValue("i");
		if(param != null) 
			try { 
				ArrayList<Feature> fs = null;
				String inputFileFormat = FilenameUtils.getExtension(param).toLowerCase();
				switch(inputFileFormat) {
				case "shp":
					fs = SHPUtil.getFeatures(param);
					break;
				case "geojson":
					fs = GeoJSONUtil.load(param);
					break;
				case "gpkg":
					fs = GeoPackageUtil.getFeatures(param);
					break;
				default:
					System.out.println("Unsuported input format: " + inputFileFormat);
				}

				if(fs.size() == 0)
					System.err.println("Input file is empty: " + param);
				else {
					if(fs.size() > 1)
						System.err.println("Input file contains more than one geometry (nb="+fs.size()+"): " + param);
					Geometry geomToCover = fs.iterator().next().getGeometry();
					sg.setGeometryToCover(geomToCover);
				}

			} catch (Exception e) {
				System.err.println("Failed reading input geometry from file: " + param);
			}

		//tol
		param = cmd.getOptionValue("tol");
		if(param != null) 
			try {
				sg.setToleranceDistance(Double.parseDouble(param));
			} catch (Exception e) { System.err.println("Failed reading parameter 'tol'. The default value will be used."); }

		//gt
		param = cmd.getOptionValue("gt");
		if(param != null)
			try {
				if(param == GridCellGeometryType.SURFACE.toString()) sg.setGridCellGeometryType(GridCellGeometryType.SURFACE);
				else if(param == GridCellGeometryType.CENTER_POINT.toString()) sg.setGridCellGeometryType(GridCellGeometryType.CENTER_POINT);
				else throw new Exception();
			} catch (Exception e) {
				System.err.println("Failed reading parameter 'gt'. The default value will be used.");
				System.err.println(param);
			}

		//output file
		String outFile = cmd.getOptionValue("o");
		if(outFile == null) outFile = Paths.get("").toAbsolutePath().toString() + "/out.gpkg";



		//build grid
		System.out.println("Build grid...");
		Collection<Feature> cells = sg.getCells();
		System.out.println(cells.size() + " grid cells built.");



		System.out.println("Save as " + outFile + "...");


		//prepare schema
		//System.out.println();
		//System.out.println(CRS.getSupportedAuthorities(true));
		//System.out.println();
		//System.out.println(CRS.getSupportedCodes("EPSG"));
		//System.out.println();

		/*
		Object authority;
		CRSAuthorityFactory factory = FactoryFinder.getCRSAuthorityFactory(authority, null);
		Set<String> codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
		String code = sg.getEPSGCode();
		CoordinateReferenceSystem crs2 = factory.createCoordinateReferenceSystem(code);
		 */


		//get output CRS
		CoordinateReferenceSystem crs = null;
		try {
			crs = CRS.decode("EPSG:" + sg.getEPSGCode());
		} catch (Exception e) {
			System.err.println("Could not find CRS with EPSG code: "+sg.getEPSGCode());
			System.err.println(e.getMessage());
			crs = null;
		}

		//make output feature type
		String gt = sg.getGridCellGeometryType()==GridCellGeometryType.SURFACE? "Polygon" : "Point";
		SimpleFeatureType ft = SimpleFeatureUtil.getFeatureType(gt, crs, new String[] { "GRD_ID:String", "X_LLC:int", "Y_LLC:int" } );

		//save according to output format
		String outputFileFormat = FilenameUtils.getExtension(outFile).toLowerCase();
		switch(outputFileFormat) {
		case "shp":
			SHPUtil.save(cells, outFile, ft);
			break;
		case "geojson":
			GeoJSONUtil.save(cells, outFile, ft.getCoordinateReferenceSystem());
			break;
		case "gpkg":
			GeoPackageUtil.save(cells, outFile, ft, true);
			//saveGPKG(cells, outFile, ft, true);
			break;
		default:
			System.out.println("Unsuported output format: " + outputFileFormat);
		}

	}

}
