java -jar ../GridMaker.jar -o outtests/out.gpkg
java -jar ../GridMaker.jar -o outtests/out.geojson
java -jar ../GridMaker.jar -o outtests/out.shp
java -jar ../GridMaker.jar -res 1000000 -o outtests/res_1000000.gpkg
java -jar ../GridMaker.jar -epsg 2154 -o outtests/epsg_2154.gpkg
java -jar ../GridMaker.jar -epsg 3857 -o outtests/epsg_3857.gpkg
java -jar ../GridMaker.jar -gt SURF -o outtests/gt_SURF.gpkg
java -jar ../GridMaker.jar -gt CPT -o outtests/gt_CPT.gpkg
java -jar ../GridMaker.jar -tol 1000000 -o outtests/tol_1000000.gpkg
java -jar ../GridMaker.jar -tol 500000 -o outtests/tol_500000.gpkg
java -jar ../GridMaker.jar -i test_grid_area.geojson -o outtests/i_geojson.gpkg
java -jar ../GridMaker.jar -i test_grid_area.shp -o outtests/i_shp.gpkg
java -jar ../GridMaker.jar -i test_grid_area.gpkg -o outtests/i_gpkg.gpkg
