# Eurostat GridMaker

[GridMaker](https://github.com/eurostat/GridMaker) produces GIS grid datasets of various resolutions covering user-defined countries or regions:

![Eurostat Grid Maker](docs/img/demo_ex/demo_ex.png)

## Quick start

1. Download [gridmaker-1.1.zip](releases/gridmaker-1.1.zip?raw=true) and unzip somewhere.
2. Run: `java -jar GridMaker.jar -res 200 -epsg 2169 -i pathTo/myRegions.geojson` to make a grid of resolution 200m for the Coordinate Reference System [EPSG:2169](https://spatialreference.org/ref/epsg/luxembourg-1930-gauss/) where `pathTo/myRegions.geojson` is the input area to be covered by the grid. You can alternativelly edit and execute *gridmaker.bat* (or *gridmaker.sh* for Linux users).

To capture the input geometry to be covered by the grid and store it as a *.geojson file, you can use [http://geojson.io/](http://geojson.io/) or any GIS software.

## Usage

### Requirements

Java 1.8 or higher is required. The java version installed, if any, can be found with `java --version` command. Recent versions of Java can be installed from [here](https://www.java.com/).

### Input parameters

The help is displayed with `java -jar GridMaker.jar -h` command.

| Parameter | Required | Description | Default value |
| ------------- | ------------- |-------------| ------|
| -h | | Show the help message |  |
| -res |  | The grid resolution (pixel size). Note that the unit of measure is expected to be the same as the one of the coordinate reference system. | 100 000 |
| -epsg |  | The EPSG code of the grid coordinate reference system. | '3035', corresponding to [ETRS89-LAEA](https://spatialreference.org/ref/epsg/etrs89-etrs-laea/). |
| -i |  | Input file containing the geometry of the region to be covered by the grid. Supported formats and file extensions: GeoJSON \(\*.geojson), SHP (\*.shp), GeoPackage (\*.gpkg). |  |
| -tol |  | A tolerance distance to keep the cells that are not too far from the specified region. | 0 |
| -gt |  | The type of grid cell geometry: The squared surface representation ('SURF') or its center point ('CPT'). | 'SURF' |
| -o |  | Output file. The supported formats and file extensions are GeoJSON (\*.geojson), SHP (\*.shp) and GeoPackage (\*.gpkg) | 'out.gpkg' |

### For coders

Install [JGiscoTools](https://github.com/eurostat/JGiscoTools/) and see the instructions [here](https://github.com/eurostat/JGiscoTools/tree/master/src/site/gridmaker).

## Showcase

[GridMaker](https://github.com/eurostat/GridMaker) is used at [Eurostat-GISCO](http://ec.europa.eu/eurostat/web/gisco) for the production of [gridded datasets](https://ec.europa.eu/eurostat/web/gisco/geodata/reference-data/grids).

[GridMaker](https://github.com/eurostat/GridMaker) supports the creation of GIS datasets representing statistical grids based on the European ETRS89-LAEA coordinate reference system ([EPSG:3035](https://spatialreference.org/ref/epsg/etrs89-etrs-laea/)), which are compliant with the [Census 2021 regulation](https://ec.europa.eu/eurostat/web/population-and-housing-census/background), [the INSPIRE regulation](https://inspire.ec.europa.eu/id/document/tg/su) and the [GEOSTAT initiative](https://www.efgs.info/geostat/).

## Support and contribution

Feel free to [ask support](https://github.com/eurostat/GridMaker/issues/new), fork the project or simply star it (it's always a pleasure). The source code is currently stored as part of [JGiscoTools](https://github.com/eurostat/JGiscoTools) repository. It is mainly based on [GeoTools](http://www.geotools.org/) and [JTS Topology Suite](https://locationtech.github.io/jts/).
