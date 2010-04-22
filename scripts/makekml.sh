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
<Placemark>
<name>Kacheln</name>
<styleUrl>#kachelStyle</styleUrl>
<MultiGeometry>
EOKMLHEAD
psql -d aio -c "SELECT ST_AsKML(the_geom) FROM tiles_europe" | tail -n+3 | head -n-2
cat << EOKMLTAIL
</MultiGeometry>
</Placemark>
</Document>
</kml>
EOKMLTAIL
) > /osm/wwwroot/aio/tiles/tiles.kml
