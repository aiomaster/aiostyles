#!/bin/bash


DATE=`date +%Y%m%d`
PLANETFILE="/osm/planet-110302.osm.pbf"
AIOPATH=/osm/garmin/aio
SCRIPTPATH=$AIOPATH/bin
TILEPATH=$AIOPATH/tmp/planet/osmtiles
LOGPATH=$AIOPATH/log/planet/$DATE
mkdir -p $LOGPATH

JAVA="/usr/lib/jvm/java-6-sun/bin/java"
JAVA_OPT="-Xmx10000M -ea -XX:MaxPermSize=256M"
MKGMAP="$JAVA $JAVA_OPT -Dlog.config=$SCRIPTPATH/logging.properties -jar $SCRIPTPATH/mkgmap.jar"
SPLITTER="$JAVA $JAVA_OPT -jar $SCRIPTPATH/splitter.jar"

TILEPATH_COMPLETE="$TILEPATH/complete"
SPLITTER_OPTIONS_COMPLETE="--mapid=70000001 --max-nodes=800000 --cache=$TILEPATH_COMPLETE/cache --geonames-file=$AIOPATH/data/geonames/cities15000.zip"

#cd $TILEPATH_COMPLETE && rm -f *.osm.gz
#/usr/bin/time -o $LOGPATH/time_splitter $SPLITTER $SPLITTER_OPTIONS_COMPLETE $PLANETFILE

STYLEPATH="$AIOPATH/data/styles"
GARMINTILES="$AIOPATH/tmp/planet/garmintiles/complete"
BASEMAP_OPTIONS="--country-name=WORLD --country-abbr=OSM --area-name=OSM_$DATE --latin1 --nsis --keep-going --transparent --add-pois-to-areas --make-all-cycleways --link-pois-to-ways --remove-short-arcs --route --index --generate-sea=floodblocker --style-file=$STYLEPATH/basemap_style --series-name=basemap_$DATE --family-id=4 --product-id=45"
/usr/bin/time -o $LOGPATH/time_mkgmap_basemap $MKGMAP $BASEMAP_OPTIONS --output-dir=$GARMINTILES/basemap $TILEPATH_COMPLETE/*.osm.gz $STYLEPATH/basemap.TYP
