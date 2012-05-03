cd /osm/garmin/aio/mkgmap/
svn update
#ant clean
ant dist
cd /osm/garmin/aio/splitter
svn update
#ant clean
ant
