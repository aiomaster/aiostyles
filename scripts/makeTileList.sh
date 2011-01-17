#!/bin/bash


TILEDIR=$1
AREASLIST=$2

SPLITVERSION=$3

SEP=':'

echo "#MD5${SEP}TILENUMBER${SEP}SPLITVERSION${SEP}DATE${SEP}BOUNDS"

for TILE in `ls ${TILEDIR}/*.img.gz`; do
	MD5=$(md5sum $TILE | cut -f1 -d ' ')
	TILENUMBER=$(echo $(basename $TILE) | cut -f1 -d'.')
	DATE=$(stat -c %Y $TILE)
	BOUNDS=$(awk "/${TILENUMBER}:/{getline;print \$3\",\"\$5}" ${AREASLIST})
	echo "$MD5$SEP$TILENUMBER$SEP$SPLITVERSION$SEP$DATE$SEP$BOUNDS" 
done


