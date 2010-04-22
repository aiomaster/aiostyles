#
#
# This is the makefile, that is used to build the All in one Garmin Map at the Openstreetmapserver and copy the result to the webdir for download.
# There are targets for every part (overlay) of the AIO-Map like basemap, fixme, addr...
# 
# 
#

SHELL := /bin/bash

AIOPATH := /osm/garmin/aio
PATH_TO_REGIONS := $(AIOPATH)


BUNDESLAENDER :=baden-wuerttemberg bayern berlin brandenburg bremen hamburg hessen mecklenburg-vorpommern niedersachsen nordrhein-westfalen rheinland-pfalz saarland sachsen-anhalt sachsen schleswig-holstein thueringen
COUNTRIES :=germany austria switzerland france italy united_kingdom albania andorra azores belarus belgium bosnia-herzegovina bulgaria croatia cyprus czech_republic denmark estonia finland greece hungary iceland isle_of_man kosovo latvia liechtenstein lithuania luxembourg macedonia malta moldova monaco montenegro netherlands norway poland portugal romania serbia slovakia slovenia spain sweden turkey ukraine
REGIONLIST := $(COUNTRIES) $(BUNDESLAENDER)
ALL_REGIONS := europe $(REGIONLIST)
CS_ALL_REGIONS := $(shell echo $(ALL_REGIONS)|sed 's/ /,/g')

# The Region should get extracted from Europe if it is part of it.
# The Regionlist defines which countries that are.
ifndef IS_PART_OF
ifeq ($(REGION),$(filter $(REGION),$(REGIONLIST)))
IS_PART_OF := europe
else
IS_PART_OF := false
endif
endif





# The splitter can use an older areas.list file from previous computations.
ifndef USE_OLD_AREAS_LIST
USE_OLD_AREAS_LIST := false
endif

ifeq ($(WORK_PARALLEL),false)
USE_CORES :=1
SHELL_EXECUTOR := ;
else
USE_CORES :=4
SHELL_EXECUTOR := &
endif



# The Printfile defines where to store all the Output of the programs. I don't want to see everything when I get a mail from cron.
ifndef PRINTFILE
PRINTFILE := $(shell tempfile)
endif

ifeq ($(REGION),$(filter $(REGION),$(BUNDESLAENDER)))
DATAPATH := /osm/geofabrik-extrakte/europe/germany/$(REGION).osm.bz2
KURZ := DE
endif

ifeq ($(REGION),$(filter $(REGION),$(COUNTRIES)))
DATAPATH := /osm/geofabrik-extrakte/europe/$(REGION).osm.bz2
KURZ := EU
endif

ifeq ($(REGION),germany)
DATAPATH := /osm/geofabrik-extrakte/europe/germany.osm.bz2
KURZ := DE
endif

ifeq ($(REGION),united_kingdom)
DATAPATH := /osm/geofabrik-extrakte/europe/great_britain.osm.bz2
KURZ := UK
endif

ifeq ($(REGION),haiti)
DATAPATH := /osm/garmin/aio/haiti/raw_data/haiti.osm.bz2
KURZ := HT
endif

ifndef REGION
REGION := europe
endif

ifeq ($(REGION),europe)
DATAPATH := /osm/geofabrik-extrakte/europe.osm.bz2
KURZ := EU
endif

# Use the first 4 numbers of the mapid as Prefix and the 5th to distinguish between the layers. The last 3 numbers are to number the tiles.
# 6324 0 023 is the first Tile of the basemap.

ifndef TILE_PREFIX
TILE_PREFIX := 6324
endif
# it is hacker style to start with 23 ;)
ifndef TILE_START
TILE_START := 023
endif

#if we use existing tiles from europe we get the right one from the postgis database
ifneq ($(IS_PART_OF),false)
ifeq ($(REGION),$(filter $(REGION),$(BUNDESLAENDER)))
REGION_TILE_INDEX := $(shell psql -d aio -c "SELECT DISTINCT tiles_$(IS_PART_OF).id FROM tiles_$(IS_PART_OF),bundeslaender WHERE ST_Intersects(tiles_$(IS_PART_OF).the_geom,bundeslaender.the_geom) AND LOWER(bundeslaender.name)=LOWER('$(REGION)');" | sed -n 's/[^0-9]*$(TILE_PREFIX)0\([0-9]*\).*/\1/gp' | tr '\n' ',' | sed 's/,$$//')
else
REGION_TILE_INDEX := $(shell psql -d aio -c "SELECT DISTINCT tiles_$(IS_PART_OF).id FROM tiles_$(IS_PART_OF),countries WHERE ST_Intersects(tiles_$(IS_PART_OF).the_geom,countries.the_geom) AND LOWER(countries.country)=LOWER(REPLACE(REPLACE('$(REGION)','_',' '),'-',' and '));" | sed -n 's/[^0-9]*$(TILE_PREFIX)0\([0-9]*\).*/\1/gp' | tr '\n' ',' | sed 's/,$$//')
endif
REGION_TILE_INDEX_PIPES := $(shell echo "$(REGION_TILE_INDEX)" | tr ',' '|')
endif


#BBOX := $(shell bzcat $(DATAPATH) |sed -n '4q;s/<bound box="/bottom=/;s/,/ left=/;s/,/ top=/;s/,/ right=/;s/" origin=.*//p')
# bottom left top right
# These choords are used for example to get all openstreetbugs in the bounding box of our region. We try to extract the bounds out of the geofabrik osm extract.
CHOORDS := $(shell bzcat $(DATAPATH) |sed -n '/<bound/{s/.*box="\([^"]*\)".*/\1/;s/,/ /gp;q}')
BOTTOM := $(word 1, $(CHOORDS) )
LEFT := $(word 2, $(CHOORDS) )
TOP := $(word 3, $(CHOORDS) )
RIGHT := $(word 4, $(CHOORDS) )
BBOX := bottom=$(BOTTOM) left=$(LEFT) top=$(TOP) right=$(RIGHT)

OSMOSIS := $(AIOPATH)/osmosis-0.31/bin/osmosis
REGIONPATH := $(PATH_TO_REGIONS)/$(REGION)
DATE := $(shell date +%Y%m%d)
WEBDIR := /osm/wwwroot/aio
GMAPTOOL := /usr/local/bin/gmt
JAVA_OPT := -Xmx8000M -ea
STYLEPATH := $(AIOPATH)/styles
TILEPATH := $(REGIONPATH)/tiles
MKGMAP := java $(JAVA_OPT) -jar $(AIOPATH)/mkgmap.jar
SPLITTER := java $(JAVA_OPT) -jar $(AIOPATH)/splitter.jar
LOGPATH := ${AIOPATH}/logfiles/${REGION}/$(DATE)

# Define dependencies of the layers.
ifeq ($(IS_PART_OF),false)
DEPENDENCY_BASEMAP := $(TILEPATH)/template.args
DEPENDENCY_ADDR := $(TILEPATH)/template.args
DEPENDENCY_FIXME := $(TILEPATH)/template.args
DEPENDENCY_MAXSPEED := $(TILEPATH)/template.args
DEPENDENCY_BOUNDARY := $(TILEPATH)/template.args
DEPENDENCY_DAMAGE := $(TILEPATH)/template.args
else
PART_META_PATH := $(PATH_TO_REGIONS)/$(IS_PART_OF)
DEPENDENCY_BASEMAP := $(PART_META_PATH)/gmapsupps/gbasemap/gmapsupp.img
DEPENDENCY_ADDR := $(PART_META_PATH)/gmapsupps/gaddr/gmapsupp.img
DEPENDENCY_FIXME := $(PART_META_PATH)/gmapsupps/gfixme/gmapsupp.img
DEPENDENCY_MAXSPEED := $(PART_META_PATH)/gmapsupps/gmaxspeed/gmapsupp.img
DEPENDENCY_BOUNDARY := $(PART_META_PATH)/gmapsupps/gboundary/gmapsupp.img
DEPENDENCY_DAMAGE := $(PART_META_PATH)/gmapsupps/gdamage/gmapsupp.img
endif

# We want other options for the overlays than for the basemaplayer containing the routing etc.
OPTIONS := --max-jobs=$(USE_CORES) --country-name=$(REGION) --country-abbr=$(KURZ) --area-name=$(KURZ) --latin1 --tdbfile --gmapsupp --nsis --keep-going
GBASEMAPOPTIONS := $(OPTIONS) --add-pois-to-areas --make-all-cycleways --link-pois-to-ways --remove-short-arcs --net --route --index  --generate-sea=polygons,no-sea-sectors,close-gaps=2000
NOBASEMAPOPTIONS := $(OPTIONS) --no-poi-address --no-sorted-roads --ignore-maxspeeds --ignore-turn-restrictions --ignore-osm-bounds --transparent

# The Tilesplitter options:
ifeq ($(USE_OLD_AREAS_LIST),true)
SPLITTER_OPTIONS := --split-file=$(TILEPATH)/areas.list --cache=$(REGIONPATH)/raw_data/splittercache --geonames-file=$(AIOPATH)/geonames/cities15000.zip
else
SPLITTER_OPTIONS := --mapid=$(TILE_PREFIX)0$(TILE_START) --max-nodes=1000000 --cache=$(REGIONPATH)/raw_data/splittercache --geonames-file=$(AIOPATH)/geonames/cities15000.zip
endif

# ifeq ($(REGION),$(filter $(REGION),sachsen berlin baden-wuerttemberg haiti))
# GBASEMAPOPTIONS := $(GBASEMAPOPTIONS) 
# #--generate-sea=extend-sea-sectors 
# endif


ifeq ($(REGION),europe)
build_exe = \
	sed -i 's/^  File "\(.*\)/  CopyFiles "$$EXEDIR\\\1 $$INSTDIR/g' $(REGIONPATH)/g$(1)/osmmap.nsi && \
	makensis -O${LOGPATH}/makensis_$(1).log $(REGIONPATH)/g$(1)/osmmap.nsi ;
else
build_exe = \
	sed 's/^  File "\(.*\)/  CopyFiles "$$EXEDIR\\\1 $$INSTDIR/g' $(REGIONPATH)/g$(1)/osmmap.nsi > $(REGIONPATH)/g$(1)/osmmap_copy.nsi && \
	makensis -O${LOGPATH}/makensis_$(1)_copy.log $(REGIONPATH)/g$(1)/osmmap_copy.nsi ; \
	(sed -i 's|^OutFile "|OutFile "$(REGIONPATH)/gmapsupps/g$(1)/|' $(REGIONPATH)/g$(1)/osmmap.nsi && \
	makensis -O${LOGPATH}/makensis_$(1).log $(REGIONPATH)/g$(1)/osmmap.nsi && \
	mv $(REGIONPATH)/gmapsupps/g$(1)/*.exe $(WEBDIR)/$(REGION)/$(DATE)/OSM-AllInOne-$(KURZ)-$(1).$(DATE).exe && \
	ln -sf $(DATE)/OSM-AllInOne-$(KURZ)-$(1).$(DATE).exe $(WEBDIR)/$(REGION)/OSM-AllInOne-$(KURZ)-$(1).exe && \
	md5sum -b $(WEBDIR)/$(REGION)/OSM-AllInOne-$(KURZ)-$(1).exe > $(WEBDIR)/$(REGION)/OSM-AllInOne-$(KURZ)-$(1).exe.md5 ) $(SHELL_EXECUTOR)
endif


tar_img_dir = \
	(tar cjf $(REGIONPATH)/release/g$(1).${DATE}.tar.bz2 -C $(REGIONPATH) g$(1) && \
	cp -f $(REGIONPATH)/release/g$(1).$(DATE).tar.bz2 $(WEBDIR)/$(REGION)/$(DATE)/ && \
	ln -sf $(DATE)/g$(1).$(DATE).tar.bz2 $(WEBDIR)/$(REGION)/g$(1).tar.bz2 && \
	md5sum -b $(WEBDIR)/$(REGION)/g$(1).tar.bz2 > $(WEBDIR)/$(REGION)/g$(1).tar.bz2.md5 ) $(SHELL_EXECUTOR)

compress_gmapsupp = \
	(bzip2 -c $(REGIONPATH)/gmapsupps/g$(1)/gmapsupp.img > $(WEBDIR)/$(REGION)/$(DATE)/g$(1).$(DATE).img.bz2 && \
	ln -sf $(DATE)/g$(1).$(DATE).img.bz2 $(WEBDIR)/$(REGION)/g$(1).img.bz2 && \
	md5sum -b $(WEBDIR)/$(REGION)/g$(1).img.bz2 > $(WEBDIR)/$(REGION)/g$(1).img.bz2.md5 ) $(SHELL_EXECUTOR)


# params:
# name,options,source
do_stuff = \
	mkdir -p $(LOGPATH) ; \
	cd $(REGIONPATH)/g$(1)/ && \
	cp $(STYLEPATH)/$(1).TYP $(REGIONPATH)/g$(1) && \
	/usr/bin/time -o $(LOGPATH)/time_mkgmap_$(1) $(MKGMAP) --style-file=$(STYLEPATH)/$(1)_style --series-name="OSM-AllInOne-$(KURZ)-$(1)" $(2) $(3) $(1).TYP 2> $(LOGPATH)/mkgmap_$(1).log && \
	{ mkdir -p $(WEBDIR)/$(REGION)/$(DATE) && \
	$(call build_exe,$(1)) \
	mv $(REGIONPATH)/g$(1)/gmapsupp.img $(REGIONPATH)/gmapsupps/g$(1)/gmapsupp.img && \
	(  $(call tar_img_dir,$(1)) \
	$(call compress_gmapsupp,$(1)) ) ; \
	echo -e "Parameters used with mkgmap to build the $(1)-Layer:\n--style-file=$(1)_style --series-name=\"OSM-AllInOne-$(KURZ)-$(1)\" $(2) $(3)\nmkgmap --version:" > $(WEBDIR)/$(REGION)/mkgmap_params_$(1) ; $(MKGMAP) --version 2>> $(WEBDIR)/$(REGION)/mkgmap_params_$(1) ; } ;


# if we reuse made tiles from bigger areas we have to copy them into our directory and make a new template.args containing only the needed (and copied) tiles
copy_tiles = \
	cp -a $(PART_META_PATH)/$(1)/$(TILE_PREFIX)*{$(REGION_TILE_INDEX)}.img $(REGIONPATH)/$(1)/ ; \
	grep -A2 -E "mapname: $(TILE_PREFIX)[0-9]($(REGION_TILE_INDEX_PIPES))" $(PART_META_PATH)/tiles/$(1)_template.args | sed 's/^--$$//g;/mapname:/h;/input-file:/{g;s/mapname: \([0-9]*\)/input-file: \1.img/}' > $(REGIONPATH)/$(1)/template.args

# make_layer takes the following parameters that differ for every layer:
# params: 	name		mkgmap_options		layernumber used for TileIDs	family-id	product-id	draw-priority	extra_opts while combine premade tiles
# example: 	basemap		$(GBASEMAPOPTIONS)	0				4		45		20		--index
ifeq ($(IS_PART_OF),false)
handle_layer = \
	sed 's/mapname: $(TILE_PREFIX)0/mapname: $(TILE_PREFIX)$(3)/g;s/description: \(.*\)/description: \1-$(1)/g' $(TILEPATH)/template.args > $(TILEPATH)/g$(1)_template.args && \
	$(call do_stuff,$(1),$(2) --family-id=$(4) --product-id=$(5) --family-name=$(1) --draw-priority=$(6),-c $(TILEPATH)/g$(1)_template.args)
else
handle_layer = \
	$(call copy_tiles,g$(1)) && \
	$(call do_stuff,$(1),$(7) --gmapsupp --nsis --family-id=$(4) --product-id=$(5) --family-name=$(1),-c $(REGIONPATH)/g$(1)/template.args)
endif


make_layer = \
	rm -f $(REGIONPATH)/g$(1)/* ; \
	echo -e "To install this stuff in Mapsource you have do execute the .exe from this directory.\nThis will start the installer, copy the *.img's to your Mapsource-map-folder and write to your registry.\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	echo -e "To load this map in QLandkarteGT you have to select the osmmap.tdb in this directory from within QLandKarteGt.\nThe Typfile in this directory gets automatically included.\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	echo -e "For generating a gmapsupp to copy to your SD-Card you need to run mkgmap like this:\njava -ea -jar mkgmap.jar --gmapsupp --family-id=$(4) --product-id=$(5) --family-name=$(1) ./*.img $(1).TYP\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	echo -e "Enjoy!\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	$(call handle_layer,$(1),$(2),$(3),$(4),$(5),$(6),$(7))

all: $(WEBDIR)/$(REGION)/gmapsupp.img.bz2 $(WEBDIR)/$(REGION)/styles.tar.bz2

structure:
	mkdir -p $(REGIONPATH)/gbasemap
	mkdir -p $(REGIONPATH)/gaddr
	mkdir -p $(REGIONPATH)/gfixme
	mkdir -p $(REGIONPATH)/gmaxspeed
	mkdir -p $(REGIONPATH)/gboundary
	mkdir -p $(REGIONPATH)/gosb
	mkdir -p $(REGIONPATH)/gcontourlines
	mkdir -p $(REGIONPATH)/raw_data/splittercache
	mkdir -p $(REGIONPATH)/raw_data/boundssplit
	mkdir -p $(REGIONPATH)/raw_data/contourlines
	mkdir -p $(REGIONPATH)/gmapsupps/gbasemap
	mkdir -p $(REGIONPATH)/gmapsupps/gaddr
	mkdir -p $(REGIONPATH)/gmapsupps/gboundary
	mkdir -p $(REGIONPATH)/gmapsupps/gfixme
	mkdir -p $(REGIONPATH)/gmapsupps/gmaxspeed
	mkdir -p $(REGIONPATH)/gmapsupps/gosb
	mkdir -p $(REGIONPATH)/gmapsupps/gcontourlines
	mkdir -p $(REGIONPATH)/tiles
	mkdir -p $(REGIONPATH)/release



# Clean the webdir and remove all directories that are older than EXPIRE date.
# Clean the release dir of the region-dirs.

ifndef EXPIRE
EXPIRE := 6
endif

clean:
	rm $(PATH_TO_REGIONS)/{$(CS_ALL_REGIONS)}/release/*.bz2 ; \
	find $(WEBDIR)/{$(CS_ALL_REGIONS)} -type d -ctime +$(EXPIRE) -exec rm -rf '{}' ';'


# If something has changed in the stylepath repack it and write it to the webdir.

$(WEBDIR)/$(REGION)/styles.tar.bz2 : $(STYLEPATH)/*/*
	tar cjf $(WEBDIR)/$(REGION)/styles.tar.bz2 -C $(STYLEPATH)/../ styles


# Generate the gmapsupp by combining all layer-gmapsupp.img-files.

$(WEBDIR)/$(REGION)/gmapsupp.img.bz2 : $(REGIONPATH)/gmapsupps/gbasemap/gmapsupp.img $(REGIONPATH)/gmapsupps/gaddr/gmapsupp.img $(REGIONPATH)/gmapsupps/gfixme/gmapsupp.img $(REGIONPATH)/gmapsupps/gmaxspeed/gmapsupp.img $(REGIONPATH)/gmapsupps/gboundary/gmapsupp.img | $(REGIONPATH)/gmapsupps/gosb/gmapsupp.img
# This is an OR Statemant in Makefilesyntax:
ifeq ($(REGION),$(filter $(REGION),$(BUNDESLAENDER)))
	cd $(REGIONPATH)/release; $(MKGMAP) --gmapsupp $(REGIONPATH)/gmapsupps/g{basemap,addr,fixme,osb,boundary,maxspeed}/gmapsupp.img
else
ifeq ($(REGION),$(filter $(REGION),haiti))
#	$(GMAPTOOL) -j -m $(DATE) -o $(REGIONPATH)/release/gmapsupp.img $(REGIONPATH)/gmapsupps/gbasemap/gmapsupp.img $(REGIONPATH)/gmapsupps/gaddr/gmapsupp.img $(REGIONPATH)/gmapsupps/gosb/gmapsupp.img $(REGIONPATH)/gmapsupps/gboundary/gmapsupp.img $(REGIONPATH)/gmapsupps/gdamage/gmapsupp.img
	cd $(REGIONPATH)/release; $(MKGMAP) --gmapsupp $(REGIONPATH)/gmapsupps/g{basemap,addr,fixme,osb,boundary,damage}/gmapsupp.img
	cp $(REGIONPATH)/release/gmapsupp.img $(WEBDIR)/$(REGION)/gmapsupp.img
else
	cd $(REGIONPATH)/release; $(MKGMAP) --gmapsupp $(REGIONPATH)/gmapsupps/g{basemap,addr,fixme,osb,boundary,maxspeed}/gmapsupp.img $(AIOPATH)/germany/gmapsupps/ghoehe/gmapsupp.img

#	$(GMAPTOOL) -j -m $(DATE) -o $(REGIONPATH)/release/gmapsupp.img $(REGIONPATH)/gmapsupps/gbasemap/gmapsupp.img $(REGIONPATH)/gmapsupps/gaddr/gmapsupp.img $(REGIONPATH)/gmapsupps/gfixme/gmapsupp.img $(REGIONPATH)/gmapsupps/gosb/gmapsupp.img $(REGIONPATH)/gmapsupps/gboundary/gmapsupp.img $(REGIONPATH)/../germany/gmapsupps/ghoehe/gmapsupp.img
endif
endif
	bzip2 -f $(REGIONPATH)/release/gmapsupp.img
	mv $(REGIONPATH)/release/gmapsupp.img.bz2 $(REGIONPATH)/release/gmapsupp.img.$(DATE).bz2
	cp -f $(REGIONPATH)/release/gmapsupp.img.$(DATE).bz2 $(WEBDIR)/$(REGION)/$(DATE)/
	ln -sf $(WEBDIR)/$(REGION)/$(DATE)/gmapsupp.img.$(DATE).bz2 $(WEBDIR)/$(REGION)/gmapsupp.img.bz2
	md5sum -b $(WEBDIR)/$(REGION)/gmapsupp.img.bz2 > $(WEBDIR)/$(REGION)/gmapsupp.img.bz2.md5 &
ifeq ($(REGION),germany)
	ncftpput -b -f ~/.ncftp/juergen gmapsupps/germany $(WEBDIR)/$(REGION)/*.bz2
endif
	echo '--------------------------------------- Ende $(REGION)' >> $(PRINTFILE);date >> $(PRINTFILE);echo "-----------------------------------" >> $(PRINTFILE)

$(REGIONPATH)/gmapsupps/gbasemap/gmapsupp.img : $(DEPENDENCY_BASEMAP)
	$(call make_layer,basemap,$(GBASEMAPOPTIONS),0,4,45,10,--index)

$(REGIONPATH)/gmapsupps/gaddr/gmapsupp.img : $(DEPENDENCY_ADDR)
	$(call make_layer,addr,$(NOBASEMAPOPTIONS),1,5,40,20)

$(REGIONPATH)/gmapsupps/gfixme/gmapsupp.img : $(DEPENDENCY_FIXME)
	$(call make_layer,fixme,$(NOBASEMAPOPTIONS),2,3,33,22)

$(REGIONPATH)/gmapsupps/gmaxspeed/gmapsupp.img : $(DEPENDENCY_MAXSPEED)
	$(call make_layer,maxspeed,$(NOBASEMAPOPTIONS),6,84,15,19)

#$(REGIONPATH)/gmapsupps/gboundary/gmapsupp.img : $(REGIONPATH)/raw_data/boundssplit/template.args
$(REGIONPATH)/gmapsupps/gboundary/gmapsupp.img : $(DEPENDENCY_BOUNDARY)
	$(call make_layer,boundary,$(NOBASEMAPOPTIONS) --process-boundary-relations,4,6,30,21)

$(REGIONPATH)/gmapsupps/gosb/gmapsupp.img : $(AIOPATH)/openstreetbugs/osbdump_latest.sql.bz2
	bzcat $(AIOPATH)/openstreetbugs/osbdump_latest.sql.bz2 | $(AIOPATH)/osbsql2osm | $(OSMOSIS) --rx - --bb $(BBOX) --nkv keyValueList="type.0" --wx $(REGIONPATH)/raw_data/osb_$(REGION).osm
	rm -f $(REGIONPATH)/gosb/*
	$(call do_stuff,osb,$(NOBASEMAPOPTIONS) --family-id=2323 --product-id=42 --family-name=osb  --mapname=$(TILE_PREFIX)3$(TILE_START) --description='Openstreetbugs' --draw-priority=23,../raw_data/osb_$(REGION).osm)

$(REGIONPATH)/gmapsupps/gdamage/gmapsupp.img : $(DEPENDENCY_DAMAGE)
	$(call make_layer,damage,$(NOBASEMAPOPTIONS),5,4242,2323,25)

$(REGIONPATH)/gmapsupps/gcontourlines/gmapsupp.img:
	cd $(REGIONPATH)/raw_data/contourlines; phyghtmap -a $(LEFT):$(BOTTOM):$(RIGHT):$(TOP) -j $(USE_CORES)
	cd $(REGIONPATH)/gcontourlines; java $(JAVA_OPT) -jar $(AIOPATH)/mkgmap.jar $(NOBASEMAPOPTIONS) --style-file=$(STYLEPATH)/masterstyle --description="contour lines" --family-id=5 --product-id=25  --series-name="OSM-AllInOne-$(KURZ)-contourlines" --family-name=contourlines --mapname=63248345 --draw-priority=100  $(REGIONPATH)/raw_data/contourlines/*.osm ;\
	makensis osmmap.nsi
	cp $(REGIONPATH)/gcontourlines/OSM-AllInOne-$(KURZ)-contourlines.exe $(WEBDIR)/$(REGION)/
	mv $(REGIONPATH)/gcontourlines/gmapsupp.img $(REGIONPATH)/gmapsupps/gcontourlines/gmapsupp.img
	bzip2 -c $(REGIONPATH)/gmapsupps/gcontourlines/gmapsupp.img > $(WEBDIR)/$(REGION)/contourlines.img.bz2
	tar cjf $(REGIONPATH)/release/gcontourlines.tar.bz2 -C $(REGIONPATH) gcontourlines
	cp -f $(REGIONPATH)/release/gcontourlines.tar.bz2 $(WEBDIR)/$(REGION)/
	md5sum -b $(WEBDIR)/$(REGION)/gcontourlines.tar.bz2 > $(WEBDIR)/$(REGION)/gcontourlines.tar.bz2.md5

$(REGIONPATH)/raw_data/boundssplit/template.args : $(TILEPATH)/template.args
	cd $(REGIONPATH)/raw_data/boundssplit/ ; java $(JAVA_OPT) -jar $(AIOPATH)/splitter.jar --mapid=$(TILE_PREFIX)5$(TILE_START)  --max-nodes=1000000 ../$(REGION)_bounds.osm

$(REGIONPATH)/tiles/template.args : $(DATAPATH)
	echo "$(REGION) Auspacken Start:" >> $(PRINTFILE);date >> $(PRINTFILE)
#	cd $(REGIONPATH)/tiles/ && rm $(REGIONPATH)/tiles/* && rm $(REGIONPATH)/raw_data/splittercache/*; bzcat $(DATAPATH) | tee \
#	>($(OSMOSIS) --rx - --way-key-value keyValueList="boundary.administrative,boundary.national,boundary.political" --used-node --write-xml ../raw_data/${REGION}_bounds.osm) \
#	| java $(JAVA_OPT) -jar $(AIOPATH)/splitter.jar --mapid=$(TILE_PREFIX)0$(TILE_START) --max-nodes=1000000 --cache=../raw_data/splittercache /dev/stdin
ifeq ($(IS_PART_OF),false)
	mkdir -p $(LOGPATH)
	cd $(TILEPATH)/ && rm $(TILEPATH)/*.osm.gz ; /usr/bin/time -o $(LOGPATH)/time_splitter bzcat $(DATAPATH) | $(SPLITTER) $(SPLITTER_OPTIONS) /dev/stdin 2> $(LOGPATH)/splitter.log
	echo "USE_OLD_AREAS_LIST=$(USE_OLD_AREAS_LIST)" >> $(LOGPATH)/time_splitter
# Set the whole path name for the tiles in template.args
	sed -i "s|input-file: \(.*\)|input-file: $(TILEPATH)/\1|g" $(TILEPATH)/template.args
endif
ifeq ($(REGION),europe)
ifeq ($(USE_OLD_AREAS_LIST),false)
	psql -d aio -c "DELETE FROM tiles_europe;"
	sed -n '/^[0-9]*:/{N;s/^\([0-9]*\):[^:]*: \(.*\),\(.*\) to \(.*\),\(.*\)/INSERT INTO tiles_europe (id,the_geom) VALUES (\1,ST_SetSRID(ST_MakeBox2D(ST_Point(\3,\2),ST_Point(\5,\4)),4326));/p}' $(TILEPATH)/areas.list | psql -d aio
	$(AIOPATH)/makekml.sh
endif
endif
