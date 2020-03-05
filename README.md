# Eurostat GridMaker

[GridMaker](https://github.com/eurostat/GridMaker) produces grids of various resolutions covering user-defined countries or regions:

![Eurostat Grid Maker](docs/img/demo_ex/demo_ex.png)

[GridMaker](https://github.com/eurostat/GridMaker) supports the creation of GIS datasets representing statistical grids based on the European ETRS89-LAEA coordinate reference system ([EPSG:3035](https://spatialreference.org/ref/epsg/etrs89-etrs-laea/)), which are compliant with the [Census 2021 regulation](https://ec.europa.eu/eurostat/web/population-and-housing-census/background), [the INSPIRE regulation](https://inspire.ec.europa.eu/id/document/tg/su) and the [GEOSTAT initiative](https://www.efgs.info/geostat/).


## Quick start

1. Download [gridmaker-1.1.zip](releases/gridmaker-1.1.zip?raw=true) and unzip somewhere.
2. Run: `java -jar GridMaker.jar -res 200 -epsg 2169 -i pathTo/myRegions.geojson` to make a grid of resolution 200m for the Coordinate Reference System [EPSG:2169](https://spatialreference.org/ref/epsg/luxembourg-1930-gauss/) where `pathTo/myRegions.geojson` is the input area to be covered by the grid. You can alternativelly edit and execute *gridmaker.bat* (or *gridmaker.sh* for Linux users).

To capture the input geometry to be covered by the grid and store it as a *.geojson file, you can use [http://geojson.io/] or any GIS software.

## Usage

### Requirements

Java 1.8 or higher is required. The java version installed, if any, can be found with `java --version` command. Recent versions of Java can be installed from [here](https://www.java.com/).

### Input parameters

The help is displayed with `java -jar GridMaker.jar -h` command.

| Parameter | Required | Description | Default value |
| ------------- | ------------- |-------------| ------|
| -h | | Show the help message |  |

TODO

### For coders

Install [JGiscoTools](https://github.com/eurostat/JGiscoTools/) and see the instructions [here](https://github.com/eurostat/JGiscoTools/tree/master/src/site/gridmaker)

## Support and contribution

Feel free to [ask support](https://github.com/eurostat/GridMaker/issues/new) or even contribute to the development of new features by [forking](https://help.github.com/en/articles/fork-a-repo) and pulling your modifications !
