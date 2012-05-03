#!/bin/bash                                                                                                                                                                                                             
cat /osm/wwwroot/aio/mkgmap_europe_basemap.log|awk '{print $5}'|grep -o -e "[^:]*"|uniq|sort| while read -r line; 
do 
echo "$line";
rm /osm/wwwroot/aio/mkgmap-errors/"$line".txt
grep "$line" /osm/wwwroot/aio/mkgmap_europe_basemap.log > /osm/wwwroot/aio/mkgmap-errors/"$line".txt
done