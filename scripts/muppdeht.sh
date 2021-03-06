#!/bin/bash

# bash script to start make with the region targets

STAMPFILE="/osm/stampfiles/aio.stamp"

LOCKFILE="/osm/garmin/aio/LOCKENPRACHT.lock"

PRINT=$(tempfile)

AIOPATH=/osm/garmin/aio

BUNDESLAENDER="baden-wuerttemberg bayern berlin brandenburg bremen hamburg hessen mecklenburg-vorpommern niedersachsen nordrhein-westfalen rheinland-pfalz saarland sachsen-anhalt sachsen schleswig-holstein thueringen"
COUNTRIES="austria switzerland france italy united_kingdom albania andorra azores belarus belgium bosnia-herzegovina bulgaria croatia cyprus czech_republic denmark estonia finland greece hungary iceland isle_of_man kosovo latvia liechtenstein lithuania luxembourg macedonia malta moldova monaco montenegro netherlands norway poland portugal romania serbia slovakia slovenia spain sweden turkey ukraine"

DOW=`date +%w`

# if it is not AiO day then exit
for i in 0 2 4 6; do
  if [ $DOW -eq $i ]; then
	incrontab -d
	exit 0
  fi
done

if [ -f $LOCKFILE ]; then
  echo "Es war gelockt: ${LOCKFILE} existiert. Datum: `date`" > ${AIOPATH}/log/muppdeht.log
  exit 0
else
  touch $LOCKFILE
  rm -f $STAMPFILE

# get the newest styles from git repository
  cd ${AIOPATH}/data/styles && git pull

# get the latest openstreetbugs
  cd ${AIOPATH}/tmp/openstreetbugs
  wget -N -q http://openstreetbugs.schokokeks.org/dumps/osbdump_latest.sql.bz2

# get the latest keepright dump
  cd ${AIOPATH}/tmp/keepright
  wget -N -q http://keepright.ipax.at/keepright_errors.txt.bz2


  cd ${AIOPATH}/bin

# you can specify extra params for generating europe if you want

EUROPEPARAMS=$1

DATE=`date +%Y%m%d`

# Jeden Montag rechne komplett neu. Sonst benutze die alten Splittergrenzen.
  if [ $DOW -ne 1 ]; then
  	EUROPEPARAMS="$EUROPEPARAMS  USE_OLD_AREAS_LIST=true"
  fi
  ionice -c 3 nice -n 19 /usr/bin/time -o ${AIOPATH}/log/europe/time_makefile_europe_$DATE make PRINTFILE=${PRINT} ${EUROPEPARAMS} REGION=europe >> ${AIOPATH}/log/logfile.log 2>> ${AIOPATH}/log/error_logfile.log
  EU_RET=$?

# Make the logfile of the european basemap run of mkgmap public

  cat ${AIOPATH}/log/europe/$DATE/mkgmap_basemap.log|sed 's|/osm/garmin/aio/tmp/regions/europe/tiles/||g' > /osm/wwwroot/aio/mkgmap_europe_basemap.log

# if europe has succeded we can extract the countries
  if [ ${EU_RET} -eq 0 ]; then

# lets do it parallel with the parallel processing shell script
  rm -r $AIOPATH/bin/ppss_dir
  echo "germany $BUNDESLAENDER $COUNTRIES" | tr ' ' '\n' | /usr/bin/time -o ${AIOPATH}/log/time_makefile_countries_$DATE ${AIOPATH}/bin/ppss -f - -p 2 -c "ionice -c 3 nice -n 19 /usr/bin/time -o ${AIOPATH}/log/\$ITEM/time_makefile_\$ITEM_$DATE make PRINTFILE=${PRINT} REGION=\$ITEM >> ${AIOPATH}/log/logfile.log"

  fi

  echo "------------------`date`---------------------" >> ${AIOPATH}/log/logfile.log
  cat ${PRINT}

  rm $LOCKFILE
  echo -e "`date`\nReturn Code of Europe make: ${EU_RET}" > $STAMPFILE
fi

rm ${PRINT}

# incrontab deamon has to read the incrontabfile again to get the new inode from our triggered file
incrontab -d


# just some stuff to work with the database:

# grep "^[^#]" europe/tiles/areas.list | sed 's/^\(.*\): \(.*\),\(.*\) to \(.*\),\(.*\)/INSERT INTO kacheln (id,boxes) VALUES (\1,ST_SetSRID(ST_MakeBox2D(ST_Point(\3, \2),ST_Point(\5, \4)),900913));/g' > tiles.sql



# CREATE TABLE kacheln (id integer);
# SELECT AddGeometryColumn('kacheln','boxes',4326,'POLYGON',2);
# grep -A1 "^[^#]" europe/tiles/areas.list | tr '\n' ' ' | sed 's/--/\n/g' | sed 's/^\(.*\):.*: \(.*\),\(.*\) to \(.*\),\(.*\)/INSERT INTO kacheln (id,boxes) VALUES (\1,ST_SetSRID(ST_MakeBox2D(ST_Point(\3, \2),ST_Point(\5,\4)),4326));/g' > tiles.sql
# psql -d aio -c "DELETE FROM kacheln;"
# psql -d aio -f tiles.sql
# psql -d aio -c "SELECT kacheln.id,countries.country FROM kacheln,countries WHERE ST_Intersects(kacheln.boxes, ST_SetSRID(countries.the_geom,4326));" -o tiles_countries

