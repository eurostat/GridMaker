# Eurostat GridMaker

[GridMaker](https://github.com/eurostat/GridMaker) is a tool to produce grids of various resolutions covering user-defined countries or regions.

![Eurostat Grid Maker](doc/img/demo_ex/demo_ex.png)

[GridMaker](https://github.com/eurostat/GridMaker) supports the creation of GIS datasets representing statistical grids based on the European ETRS89-LAEA coordinate reference system ([EPSG:3035](https://spatialreference.org/ref/epsg/etrs89-etrs-laea/)), which are compliant with the [Census 2021 regulation](https://ec.europa.eu/eurostat/web/population-and-housing-census/background), [the INSPIRE regulation](https://inspire.ec.europa.eu/id/document/tg/su) and the [GEOSTAT initiative](https://www.efgs.info/geostat/).

## Usage

### As a program

(TODO: Document)

### For Java coders

[GridMaker](https://github.com/eurostat/GridMaker) is currently not deployed on a maven repository but you can quickly download, compile and install it locally with:

```
git clone https://github.com/eurostat/GridMaker.git
cd GridMaker
mvn clean install
```

and then use it in your Java project as a maven dependency:

```
<dependency>
	<groupId>eu.europa.ec.eurostat</groupId>
	<artifactId>GridMaker</artifactId>
	<version>1.0</version>
</dependency>
```

Here is an example of grid creation process:

```java

(TODO: Add example)

```

(TODO: Add link to javadoc.)

## Support and contribution

Feel free to [ask support](https://github.com/eurostat/GridMaker/issues/new) or even contribute to the development of new features by [forking](https://help.github.com/en/articles/fork-a-repo) and pulling your modifications !
