BEGIN{
FS = "\t" # set Field separator to tabulator
CONVFMT = "%0.7f" #set output format of the choords to 7 digits after dot and leading zero
getline #Remove first line containing the table head
# print the xml head
print "<?xml version='1.0' encoding='UTF-8'?>\n<osm version='0.6' generator='keepright2osm'>"
}
{
# escape some characters for xml
gsub("&","\\&amp;")
gsub("'","\\&apos;")
gsub("\"","\\&quot;")
gsub("<","\\&lt;")
gsub(">","\\&gt;")
# generate a full osm node; divide choords by 10000000
print "<node id='"$2"' timestamp='"$11"' visible='true' version='1' lat='"$12/10000000"' lon='"$13/10000000"'>\n <tag k='schema' v='"$1"' />\n <tag k='error_type' v='"$3"' />\n <tag k='error_name' v='"$4"' />\n <tag k='object_type' v='"$5"' />\n <tag k='object_id' v='"$6"' />\n <tag k='state' v='"$7"' />\n <tag k='description' v='"$8"' />\n <tag k='first_occurrence' v='"$9"' />\n <tag k='last_checked' v='"$10"' />\n <tag k='comment' v='"$14"' />\n <tag k='comment_timestamp' v='"$15"' />\n</node>"
}
END{
#close the osm tag
print "</osm>"
}
