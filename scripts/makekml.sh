#!/bin/bash

(
cat << EOKMLHEAD
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<kml xmlns="http://earth.google.com/kml/2.2" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:xal="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0">
<Document>
    <open>true</open>
    <Style id="kachelStyle">
      <LineStyle>
        <color>7f2581e3</color>
        <width>4</width>
      </LineStyle>
      <PolyStyle>
        <color>2025b1e3</color>
      </PolyStyle>
    </Style>
EOKMLHEAD
psql -d aio -c "SELECT id,ST_AsKML(the_geom) FROM $1" | tail -n+3 | head -n-2 | awk -F\| '{gsub(/[[:space:]]*/,"",$1); print "<Placemark>\n<name>"$1"</name>\n<styleUrl>#kachelStyle</styleUrl>\n"$2"\n</Placemark>"}'
cat << EOKMLTAIL
</Document>
</kml>
EOKMLTAIL
) > /osm/wwwroot/aio/tiles/$1.kml

