#
#
# This is the makefile, that is used to build the All in one Garmin Map at the Openstreetmapserver and copy the result to the webdir for download.
# There are targets for every part (overlay) of the AIO-Map like basemap, fixme, addr...
# 
# 
#

SHELL := /bin/bash

AIOPATH := /osm/garmin/aio
PATH_TO_REGIONS := $(AIOPATH)/regions

# default region is europe
ifndef REGION
REGION := europe
endif

# default abbreviation is the whole Region
KURZ:=$(REGION)

BUNDESLAENDER :=baden-wuerttemberg bayern berlin brandenburg bremen hamburg hessen mecklenburg-vorpommern niedersachsen nordrhein-westfalen rheinland-pfalz saarland sachsen-anhalt sachsen schleswig-holstein thueringen
COUNTRIES :=germany austria switzerland france italy united_kingdom albania andorra azores belarus belgium bosnia-herzegovina bulgaria croatia cyprus czech_republic denmark estonia finland greece hungary iceland isle_of_man kosovo latvia liechtenstein lithuania luxembourg macedonia malta moldova monaco montenegro netherlands norway poland portugal romania serbia slovakia slovenia spain sweden turkey ukraine
REGIONLIST := $(COUNTRIES) $(BUNDESLAENDER)
ALL_REGIONS :=europe $(REGIONLIST) canary_islands haiti
CS_ALL_REGIONS := $(shell echo $(ALL_REGIONS)|tr ' ' ',')


LAYER:=gbasemap,gaddr,gfixme,gmaxspeed,gosb,gboundary,gkeepright,gdamage,gcontourlines,gvelomap


# The Region should get extracted from Europe if it is part of it.
# The Regionlist defines which countries that are.
ifndef IS_PART_OF
ifeq ($(REGION),$(filter $(REGION),$(REGIONLIST)))
IS_PART_OF := europe
else
IS_PART_OF := false
endif
endif


# manage family- and product IDs

# for each region there is a new FID so there is no colllision if you install multiple regions in mapsource
# we add to each FID simply the index of the region in the regionlist of this makefile
REGION_LIST_INDEX:=$(shell i=0;for region in $(ALL_REGIONS); do if [ $$region = "$(REGION)" ];then echo $$i; break; else let i++; fi;done)

VELOMAP_FID:=$(shell expr 6621 + $(REGION_LIST_INDEX))
VELOMAP_PID:=2
BASEMAP_FID:=$(shell expr 7000 + $(REGION_LIST_INDEX))
BASEMAP_PID:=3
ADDR_FID:=$(shell expr 7400 + $(REGION_LIST_INDEX))
ADDR_PID:=3
FIXME_FID:=$(shell expr 7800 + $(REGION_LIST_INDEX))
FIXME_PID:=4
MAXSPEED_FID:=$(shell expr 8200 + $(REGION_LIST_INDEX))
MAXSPEED_PID:=3
OSB_FID:=$(shell expr 8600 + $(REGION_LIST_INDEX))
OSB_PID:=4
BOUNDARY_FID:=$(shell expr 9000 + $(REGION_LIST_INDEX))
BOUNDARY_PID:=3
KEEPRIGHT_FID:=$(shell expr 9400 + $(REGION_LIST_INDEX))
KEEPRIGHT_PID:=4
DAMAGE_FID:=$(shell expr 4000 + $(REGION_LIST_INDEX))
DAMAGE_PID:=5
CONTOURLINES_FID:=$(shell expr 3000 + $(REGION_LIST_INDEX))
CONTOURLINES_PID:=6


# if we want to start some compressing tasks in a subshell to work parallel we use & instead of ; to separate shell commands
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
endif

ifeq ($(REGION),$(filter $(REGION),$(COUNTRIES)))
DATAPATH := /osm/geofabrik-extrakte/europe/$(REGION).osm.bz2
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
DATAPATH := /osm/garmin/aio/regions/haiti/raw_data/haiti.osm.bz2
KURZ := HT
endif

ifeq ($(REGION),canary_islands)
DATAPATH := /osm/geofabrik-extrakte/africa/canary_islands.osm.bz2
KURZ := CI
endif

ifeq ($(REGION),europe)
DATAPATH := /osm/geofabrik-extrakte/europe.osm.bz2
KURZ := EU
endif

# Use the first 4 numbers of the mapid as template for the Family-Id and the 5th as template for the Product-Id of the map. The last 3 numbers are to number the tiles.

ifndef TILE_PREFIX
TILE_PREFIX := 6621
endif
ifndef TILE_START
TILE_START := 001
endif

#if we use existing tiles from europe we get the right one from the postgis database
ifneq ($(IS_PART_OF),false)
ifeq ($(REGION),$(filter $(REGION),$(BUNDESLAENDER)))
REGION_TILE_INDEX=$(shell psql -d aio -c "SELECT DISTINCT tiles$(1)$(IS_PART_OF).id FROM tiles$(1)$(IS_PART_OF),bundeslaender WHERE ST_Intersects(tiles$(1)$(IS_PART_OF).the_geom,bundeslaender.the_geom) AND LOWER(bundeslaender.name)=LOWER('$(REGION)');" | sed -n 's/[^0-9]*$(TILE_PREFIX)0\([0-9]*\).*/\1/gp' | tr '\n' ',' | sed 's/,$$//')
else
REGION_TILE_INDEX=$(shell psql -d aio -c "SELECT DISTINCT tiles$(1)$(IS_PART_OF).id FROM tiles$(1)$(IS_PART_OF),countries WHERE ST_Intersects(tiles$(1)$(IS_PART_OF).the_geom,countries.the_geom) AND LOWER(countries.country)=LOWER(REPLACE(REPLACE('$(REGION)','_',' '),'-',' and '));" | sed -n 's/[^0-9]*$(TILE_PREFIX)0\([0-9]*\).*/\1/gp' | tr '\n' ',' | sed 's/,$$//')
endif
REGION_TILE_INDEX_PIPES=$(shell echo "$(call REGION_TILE_INDEX,$(1))" | tr ',' '|')
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

OSMOSIS := $(AIOPATH)/osmosis-0.35.1/bin/osmosis
REGIONPATH := $(PATH_TO_REGIONS)/$(REGION)
DATE := $(shell date +%Y%m%d)
WEBDIR := /osm/wwwroot/aio
WEBREGIONDIR := $(WEBDIR)/regions/$(REGION)
GMAPTOOL := /usr/local/bin/gmt
JAVA_OPT := -Xmx8000M -ea -XX:MaxPermSize=256M
STYLEPATH := $(AIOPATH)/styles
TILEPATH := $(REGIONPATH)/tiles
MKGMAP := java $(JAVA_OPT) -Dlog.config=$(AIOPATH)/logging.properties -jar $(AIOPATH)/mkgmap.jar
MKGMAP_basemap := $(MKGMAP)
MKGMAP_addr := $(MKGMAP)
MKGMAP_fixme := $(MKGMAP)
MKGMAP_maxspeed := $(MKGMAP)
MKGMAP_osb := $(MKGMAP)
MKGMAP_boundary := $(MKGMAP)
MKGMAP_keepright := $(MKGMAP)
MKGMAP_damage := $(MKGMAP)
MKGMAP_contourlines := $(MKGMAP)
MKGMAP_velomap := java $(JAVA_OPT) -Dlog.config=$(AIOPATH)/logging.properties -jar $(AIOPATH)/styles/mkgmap_velo.jar
SPLITTER := java $(JAVA_OPT) -jar $(AIOPATH)/splitter.jar
LOGPATH := ${AIOPATH}/logfiles/${REGION}/$(DATE)

# Define dependencies of the layers.
ifeq ($(IS_PART_OF),false)
DEPENDENCY_VELOMAP := $(TILEPATH)/template.args
DEPENDENCY_BASEMAP := $(TILEPATH)/template.args
DEPENDENCY_ADDR := $(REGIONPATH)/raw_data/addrsplit/template.args
DEPENDENCY_FIXME := $(TILEPATH)/template.args
DEPENDENCY_MAXSPEED := $(REGIONPATH)/raw_data/maxspeedsplit/template.args
DEPENDENCY_BOUNDARY := $(REGIONPATH)/raw_data/boundssplit/template.args
DEPENDENCY_KEEPRIGHT := $(REGIONPATH)/raw_data/keeprightsplit/template.args
DEPENDENCY_DAMAGE := $(TILEPATH)/template.args
else
PART_META_PATH := $(PATH_TO_REGIONS)/$(IS_PART_OF)
DEPENDENCY_VELOMAP := $(PART_META_PATH)/gmapsupps/gvelomap/gmapsupp.img
DEPENDENCY_BASEMAP := $(PART_META_PATH)/gmapsupps/gbasemap/gmapsupp.img
DEPENDENCY_ADDR := $(PART_META_PATH)/gmapsupps/gaddr/gmapsupp.img
DEPENDENCY_FIXME := $(PART_META_PATH)/gmapsupps/gfixme/gmapsupp.img
DEPENDENCY_MAXSPEED := $(PART_META_PATH)/gmapsupps/gmaxspeed/gmapsupp.img
DEPENDENCY_BOUNDARY := $(PART_META_PATH)/gmapsupps/gboundary/gmapsupp.img
DEPENDENCY_KEEPRIGHT := $(PART_META_PATH)/gmapsupps/gkeepright/gmapsupp.img
DEPENDENCY_DAMAGE := $(PART_META_PATH)/gmapsupps/gdamage/gmapsupp.img
endif

# We want other options for the overlays than for the basemaplayer containing the routing etc.
OPTIONS := --max-jobs=$(USE_CORES) --country-name=$(REGION) --country-abbr=$(KURZ) --area-name=$(KURZ)_$(DATE) --latin1 --tdbfile --gmapsupp --nsis --keep-going --transparent
GBASEMAPOPTIONS := $(OPTIONS) --add-pois-to-areas --make-all-cycleways --link-pois-to-ways --remove-short-arcs --route --index  --generate-sea=polygons,no-sea-sectors,close-gaps=2000
NOBASEMAPOPTIONS := $(OPTIONS) --no-poi-address --no-sorted-roads --ignore-maxspeeds --ignore-turn-restrictions --ignore-osm-bounds 
VELOMAPOPTIONS := $(OPTIONS) --generate-sea=polygons,extend-sea-sectors,close-gaps=6000 --reduce-point-density=3.2 --reduce-point-density-polygon=8 --suppress-dead-end-nodes --index --adjust-turn-headings --add-pois-to-areas --ignore-maxspeeds --link-pois-to-ways --ignore-turn-restrictions --remove-short-arcs=4 --route --description="VeloMap_$(KURZ)"


# The splitter can use an older areas.list file from previous computations by giving USE_OLD_AREAS_LIST=true. Default is to split new.
ifndef USE_OLD_AREAS_LIST
USE_OLD_AREAS_LIST:=false
else
ifeq ($(USE_OLD_AREAS_LIST),true)
ifndef AREAS_LIST_FILE
AREAS_LIST_FILE:=$(TILEPATH)/areas.list
endif
# We have to ensure that the areas.list file exist. if not we split completely new.
override USE_OLD_AREAS_LIST:=$(shell if test -f $(AREAS_LIST_FILE); then echo true; else echo false; fi)
endif
endif

# The Tilesplitter options:
GEONAMES:=--geonames-file=$(AIOPATH)/geonames/cities15000.zip
SPLITTER_CACHE:=$(REGIONPATH)/raw_data/splittercache
ifeq ($(USE_OLD_AREAS_LIST),true)
SPLITTER_OPTIONS := --split-file=$(AREAS_LIST_FILE) --cache=$(SPLITTER_CACHE)/basemap $(GEONAMES)
SPLITTER_OPTIONS_BOUNDARY := --split-file=$(REGIONPATH)/raw_data/boundssplit/areas.list --cache=$(SPLITTER_CACHE)/bounds $(GEONAMES)
SPLITTER_OPTIONS_KEEPRIGHT := --split-file=$(REGIONPATH)/raw_data/keeprightsplit/areas.list --cache=$(SPLITTER_CACHE)/keepright $(GEONAMES)
SPLITTER_OPTIONS_MAXSPEED := --split-file=$(REGIONPATH)/raw_data/maxspeedsplit/areas.list --cache=$(SPLITTER_CACHE)/maxspeed $(GEONAMES)
SPLITTER_OPTIONS_ADDR := --split-file=$(REGIONPATH)/raw_data/addrsplit/areas.list --cache=$(SPLITTER_CACHE)/addr $(GEONAMES)
else
SPLITTER_OPTIONS := --mapid=$(TILE_PREFIX)0$(TILE_START) --max-nodes=1000000 --cache=$(SPLITTER_CACHE)/basemap $(GEONAMES)
SPLITTER_OPTIONS_BOUNDARY := --mapid=$(TILE_PREFIX)0$(TILE_START) --max-nodes=1000000 --cache=$(SPLITTER_CACHE)/bounds $(GEONAMES)
SPLITTER_OPTIONS_KEEPRIGHT := --mapid=$(TILE_PREFIX)0$(TILE_START) --max-nodes=200000 --cache=$(SPLITTER_CACHE)/keepright $(GEONAMES)
SPLITTER_OPTIONS_MAXSPEED := --mapid=$(TILE_PREFIX)0$(TILE_START) --max-nodes=500000 --cache=$(SPLITTER_CACHE)/maxspeed $(GEONAMES)
SPLITTER_OPTIONS_ADDR := --mapid=$(TILE_PREFIX)0$(TILE_START) --max-nodes=500000 --cache=$(SPLITTER_CACHE)/addr $(GEONAMES)
endif

# You can specify different Compressors for the result
# Default is gzip
# ...obsolete!
COMPRESSOR:=gzip

ifeq ($(COMPRESSOR),zip)
COMPRESS_SUFFIX:=zip
endif
ifeq ($(COMPRESSOR),gzip)
COMPRESS_SUFFIX:=gz
endif
ifeq ($(COMPRESSOR),bzip2)
COMPRESS_SUFFIX:=bz2
endif
ifeq ($(COMPRESSOR),p7zip)
COMPRESS_SUFFIX:=7z
endif

# this is used for all compressions now:
COMPR:=7z a -l -tzip -mmt=$(USE_CORES) -mx=1
COMPRESS_SUFFIX:=zip


move_and_symlink = \
	mv $(REGIONPATH)/release/$(2).$(DATE).$(3) $(WEBREGIONDIR)/$(DATE)/ && \
	ln -sf ../$(DATE)/$(2).$(DATE).$(3) $(WEBREGIONDIR)/$(1)/$(2).$(3) && \
	md5sum -b $(WEBREGIONDIR)/$(1)/$(2).$(3) > $(WEBREGIONDIR)/$(1)/$(2).$(3).md5 ; \
	if [ "$(3)" = "img.$(COMPRESS_SUFFIX)" ]; then ln -sf ../../regions/$(REGION)/$(DATE)/$(2).$(DATE).$(3) $(WEBDIR)/$(1)/gps_ready/$(2).$(3) ; else ln -sf ../../regions/$(REGION)/$(DATE)/$(2).$(DATE).$(3) $(WEBDIR)/$(1)/mapsource/$(2).$(3) ; fi

# obsolete if you can include the whole directory in one archive without tar
tar_img_dir = \
	tar -I $(COMPRESSOR) -cf $(REGIONPATH)/release/$(1)_$(REGION).$(DATE).tar.$(COMPRESS_SUFFIX) -C $(REGIONPATH)/release $(1)_$(REGION) && \
	$(call move_and_symlink,$(1),$(1)_$(REGION),tar.$(COMPRESS_SUFFIX))

zip_img_dir = \
	ln -sfT $(REGIONPATH)/g$(1) $(REGIONPATH)/release/$(1)_$(REGION) && \
	cd $(REGIONPATH)/release/ && \
	$(COMPR) $(REGIONPATH)/release/$(1)_$(REGION).$(DATE).zip $(1)_$(REGION) && \
	$(call move_and_symlink,$(1),$(1)_$(REGION),$(COMPRESS_SUFFIX))

compress_gmapsupp = \
	cd $(REGIONPATH)/gmapsupps/g$(1) && \
	($(COMPR) $(REGIONPATH)/release/$(1)_$(REGION).$(DATE).img.$(COMPRESS_SUFFIX) gmapsupp.img && \
	$(call move_and_symlink,$(1),$(1)_$(REGION),img.$(COMPRESS_SUFFIX)) ) $(SHELL_EXECUTOR)



#ifeq ($(REGION),europe)
build_exe = \
	sed -i 's/^  File "\(.*\)/  CopyFiles "$$EXEDIR\\\1 $$INSTDIR/g;/!define INSTALLER_NAME/s/"\([^"]*\)"/"\1_setup"/' $(REGIONPATH)/g$(1)/*.nsi && \
	makensis -O${LOGPATH}/makensis_$(1).log $(REGIONPATH)/g$(1)/*.nsi
#else
#build_exe = \
#	sed 's/^  File "\(.*\)/  CopyFiles "$$EXEDIR\\\1 $$INSTDIR/g' $(REGIONPATH)/g$(1)/osmmap.nsi > $(REGIONPATH)/g$(1)/osmmap_copy.nsi && \
#	makensis -O${LOGPATH}/makensis_$(1)_copy.log $(REGIONPATH)/g$(1)/osmmap_copy.nsi ; \
#	(sed -i 's|^OutFile "|OutFile "$(REGIONPATH)/gmapsupps/g$(1)/|' $(REGIONPATH)/g$(1)/osmmap.nsi && \
#	makensis -O${LOGPATH}/makensis_$(1).log $(REGIONPATH)/g$(1)/osmmap.nsi && \
#	mv $(REGIONPATH)/gmapsupps/g$(1)/*.exe $(WEBREGIONDIR)/$(DATE)/OSM-AllInOne-$(KURZ)-$(1).$(DATE).exe && \
#	ln -sf $(DATE)/OSM-AllInOne-$(KURZ)-$(1).$(DATE).exe $(WEBREGIONDIR)/$(1)/OSM-AllInOne-$(KURZ)-$(1).exe && \
#	md5sum -b $(WEBREGIONDIR)/OSM-AllInOne-$(KURZ)-$(1).exe > $(WEBREGIONDIR)/$(1)/OSM-AllInOne-$(KURZ)-$(1).exe.md5 ) $(SHELL_EXECUTOR)
#endif

pack_results = \
	mkdir -p $(WEBREGIONDIR)/$(1) && \
	{ mkdir -p $(WEBREGIONDIR)/$(DATE) && \
	$(call build_exe,$(1)) ; \
	mv $(REGIONPATH)/g$(1)/gmapsupp.img $(REGIONPATH)/gmapsupps/g$(1)/gmapsupp.img && \
	(  ( $(call zip_img_dir,$(1)) ) $(SHELL_EXECUTOR) \
	   ($(call compress_gmapsupp,$(1)) ) $(SHELL_EXECUTOR) \
	) ; }

# params:
# name,options,source
do_stuff = \
	mkdir -p $(LOGPATH) ; \
	cd $(REGIONPATH)/g$(1) && \
	cp $(STYLEPATH)/$(1).TYP $(REGIONPATH)/g$(1) && \
	$(GMAPTOOL) -wy $(4),$(5) $(1).TYP ; \
	mv $(1).TYP `echo $(1)|sed 's/^\(.\{3\}\).*/\1/'`$(4)$(5).TYP ; \
	/usr/bin/time -o $(LOGPATH)/time_mkgmap_$(1) $(MKGMAP_$(1)) --style-file=$(STYLEPATH)/$(1)_style --series-name="$(6)" --overview-mapname=osm$(4)$(5) --overview-mapnumber=$(4)$(5)000 --family-id=$(4) --product-id=$(5) --family-name=$(1) $(2) $(3) *.TYP && \
	mv $(REGIONPATH)/g$(1)/mkgmap.log $(LOGPATH)/mkgmap_$(1).log ; \
	if [ "$(1)" = "velomap" -o "$(1)" = "basemap" ];then $(GMAPTOOL) -wn $(REGIONPATH)/g$(1)/*.img;fi ; \
	echo -e "Parameters used with mkgmap to build the $(1)-Layer:\n--style-file=$(1)_style $(2) $(3)\nmkgmap --version:" > $(WEBREGIONDIR)/$(1)/mkgmap_params_$(1) ; $(MKGMAP_$(1)) --version 2>> $(WEBREGIONDIR)/$(1)/mkgmap_params_$(1) ; \
	$(call pack_results,$(1)) ;



# if we reuse made tiles from bigger areas we have to copy them into our directory and make a new template.args containing only the needed (and copied) tiles
# Rename the imgs to match the family-id schema and change the mapid with gmaptool to prevent mapsource from crashing
copy_tiles = \
	cp -a $(PART_META_PATH)/$(1)/*{$(call REGION_TILE_INDEX,$(2))}.img $(REGIONPATH)/$(1)/ ; \
	cd $(REGIONPATH)/$(1)/ ; \
	for i in `ls *.img`;do MID=`echo $$i|sed 's/^[0-9]\{5\}\([0-9]*\)\.img/$(3)$(4)\1/g'` ; $(GMAPTOOL) -we $$MID $$i ; mv $$i $$MID.img ; done ; \
	grep -A2 -E "mapname: [0-9]{5}($(call REGION_TILE_INDEX_PIPES,$(2)))" $(PART_META_PATH)/tiles/$(1)_template.args | awk --posix '/mapname/{sub("[0-9]{5}","$(3)$(4)",$$2);mn=$$2;print;getline;print;getline;print $$1" "mn".img\n"}' > $(REGIONPATH)/$(1)/template.args

# bring a given areas.list to a specified postgis database table

areas_to_db = \
	psql -d aio -c "DELETE FROM $(2);" ; \
	sed -n '/^[0-9]*:/{N;s/^\([0-9]*\):[^:]*: \(.*\),\(.*\) to \(.*\),\(.*\)/INSERT INTO $(2) (id,the_geom) VALUES (\1,ST_SetSRID(ST_MakeBox2D(ST_Point(\3,\2),ST_Point(\5,\4)),4326));/p}' $(1) | psql -d aio


ifeq ($(REGION),europe)
ifeq ($(USE_OLD_AREAS_LIST),false)
define prepare_areas
	$(call areas_to_db,$(1)/areas.list,tiles$(2)europe)
	$(AIOPATH)/makekml.sh tiles$(2)europe
endef
endif
endif

define process_splitter_files
# Set the full path name for the tiles in template.args
	sed -i "s|input-file: \(.*\)|input-file: $(1)/\1|g" $(1)/template.args
	$(call prepare_areas,$(1),$(2))
endef

# make_layer takes the following parameters $(1)-$(9) that differ for every layer:
# params: 	name		mkgmap_options		series-name	family-id	product-id	draw-priority	template.args						PG_TABLE_SELECTOR	extra_opts while combine premade tiles
# example1: 	basemap		$(GBASEMAPOPTIONS)	0				4		45		20		$(TILEPATH)/template.args				_			--index --route
# example2:	boundary	$(NOBASEMAPOPTIONS)	4				6		30		21		$(REGIONPATH)/raw_data/boundssplit/template.args	_boundary_
ifeq ($(IS_PART_OF),false)
handle_layer = \
	sed 's/mapname: $(TILE_PREFIX)[0-9]/mapname: $(4)$(5)/g;s/description: \(.*\)/description: \1-$(1)/g' $(7) > $(TILEPATH)/g$(1)_template.args && \
	$(call do_stuff,$(1),$(2) --draw-priority=$(6),-c $(TILEPATH)/g$(1)_template.args,$(4),$(5),$(3))
else
handle_layer = \
	$(call copy_tiles,g$(1),$(8),$(4),$(5)) && \
	$(call do_stuff,$(1),$(9) --gmapsupp --nsis,-c $(REGIONPATH)/g$(1)/template.args,$(4),$(5),$(3))
endif


make_layer = \
	rm -f $(REGIONPATH)/g$(1)/* ; \
	echo -e "To install this stuff in Mapsource you have do execute the .exe from this directory.\nThis will start the installer, copy the *.img's to your Mapsource-map-folder and write to your registry.\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	echo -e "To load this map in QLandkarteGT you have to select the osmmap.tdb in this directory from within QLandKarteGt.\nThe Typfile in this directory gets automatically included.\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	echo -e "For generating a gmapsupp to copy to your SD-Card you need to run mkgmap like this:\njava -ea -jar mkgmap.jar --gmapsupp --family-id=$(4) --product-id=$(5) --family-name=$(1) ./*.img $(1).TYP\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	echo -e "Enjoy!\n" >> $(REGIONPATH)/g$(1)/HOWTO.txt ; \
	$(call handle_layer,$(1),$(2),$(3),$(4),$(5),$(6),$(7),$(8),$(9))

all: $(REGIONPATH)/gmapsupps/gvelomap/gmapsupp.img $(REGIONPATH)/gmapsupps/gmapsupp/gmapsupp.img $(WEBREGIONDIR)/styles.tar.bz2


define make_dirs
	mkdir -p $(1)/{$(LAYER)}
	mkdir -p $(1)/raw_data/{boundssplit,keeprightsplit,maxspeedsplit,addrsplit,fixme,osb,contourlines}
	mkdir -p $(1)/raw_data/splittercache/{basemap,bounds,keepright,addr,maxspeed,fixme,osb}
	mkdir -p $(1)/gmapsupps/{$(LAYER),gmapsupp}
	mkdir -p $(1)/tiles
	mkdir -p $(1)/release
endef

# create the structure for a given region

structure:
	$(call make_dirs,$(REGIONPATH))

# create the structure for all known regions

restructure:
	$(call make_dirs,$(PATH_TO_REGIONS)/{$(CS_ALL_REGIONS)})


# Clean the webdir and remove all directories that are older than EXPIRE date.
# Clean the release dir of the region-dirs.

ifndef EXPIRE
EXPIRE := 6
endif

clean:
	rm -f $(PATH_TO_REGIONS)/{$(CS_ALL_REGIONS)}/release/*.gz ; \
	find $(WEBDIR)/regions/{$(CS_ALL_REGIONS)} -type d -ctime +$(EXPIRE) -exec rm -rf '{}' ';'

# Clean all stuff not directly needed in region dirs
# Warning you need the imgs of europe to create the other subcountries! 
extreme_clean: clean
	rm -f $(PATH_TO_REGIONS)/{$(CS_ALL_REGIONS)}/{$(LAYER)}/*.img ; \
	rm -f $(PATH_TO_REGIONS)/{$(CS_ALL_REGIONS)}/tiles/*.osm.gz

	

# If something has changed in the stylepath repack it and write it to the webdir.
$(WEBREGIONDIR)/styles.tar.bz2 : $(STYLEPATH)/*/*
	tar cjf $(WEBREGIONDIR)/styles.tar.bz2 -C $(STYLEPATH)/../ styles

# introduce a system of symlinks to give a more structured access to the downloadable files

define link_to_webdir
	for i in $(ALL_REGIONS); do ln -sf ../../regions/$$i/$(1)/$(1)_$$i.img.$(COMPRESS_SUFFIX) $(WEBDIR)/$(1)/gps_ready/$(1)_$$i.img.$(COMPRESS_SUFFIX) ; ln -sf ../../regions/$$i/$(1)/$(1)_$$i.$(COMPRESS_SUFFIX) $(WEBDIR)/$(1)/mapsource/$(1)_$$i.$(COMPRESS_SUFFIX) ; done
endef

webdir_structure:
	mkdir -p $(WEBDIR)/{gmapsupp,velomap,basemap,addr,fixme,keepright,osb,maxspeed,boundary}/{gps_ready,mapsource}
	$(call link_to_webdir,gmapsupp) && rm -rf $(WEBDIR)/gmapsupp/mapsource
	$(call link_to_webdir,velomap)
	$(call link_to_webdir,basemap)
	$(call link_to_webdir,addr)
	$(call link_to_webdir,fixme)
	$(call link_to_webdir,keepright)
	$(call link_to_webdir,osb)
	$(call link_to_webdir,maxspeed)
	$(call link_to_webdir,boundary)

# to make a new tiles table for example for the keepright splits of europe do
# make TABLE_SELECTOR=_keepright_ REGION=europe postgis_tiles_table
TABLE_SELECTOR:=_
postgis_tiles_table:
	psql -d aio -c "CREATE TABLE tiles$(TABLE_SELECTOR)$(REGION) (id integer);"
	psql -d aio -c "SELECT AddGeometryColumn('tiles$(TABLE_SELECTOR)$(REGION)','the_geom',4326,'POLYGON',2);"


# Generate the gmapsupp by combining all layer-gmapsupp.img-files.

$(REGIONPATH)/gmapsupps/gmapsupp/gmapsupp.img : $(REGIONPATH)/gmapsupps/gaddr/gmapsupp.img $(REGIONPATH)/gmapsupps/gfixme/gmapsupp.img $(REGIONPATH)/gmapsupps/gmaxspeed/gmapsupp.img $(REGIONPATH)/gmapsupps/gboundary/gmapsupp.img $(REGIONPATH)/gmapsupps/gbasemap/gmapsupp.img | $(REGIONPATH)/gmapsupps/gosb/gmapsupp.img $(REGIONPATH)/gmapsupps/gkeepright/gmapsupp.img
# This is an OR Statemant in Makefilesyntax:
ifeq ($(REGION),$(filter $(REGION),$(BUNDESLAENDER) $(COUNTRIES) europe))
# include contourlines of germany if REGION is germany or europe
ifeq ($(REGION),$(filter $(REGION),germany europe))
	cd $(REGIONPATH)/gmapsupps/gmapsupp ; $(MKGMAP) --gmapsupp $(REGIONPATH)/gmapsupps/g{basemap,addr,fixme,osb,boundary,maxspeed,keepright}/gmapsupp.img $(PATH_TO_REGIONS)/germany/gmapsupps/ghoehe/gmapsupp.img
else
	cd $(REGIONPATH)/gmapsupps/gmapsupp ; $(MKGMAP) --gmapsupp $(REGIONPATH)/gmapsupps/g{basemap,addr,fixme,osb,boundary,maxspeed,keepright}/gmapsupp.img
endif
else
ifeq ($(REGION),$(filter $(REGION),haiti))
	cd $(REGIONPATH)/gmapsupps/gmapsupp ; $(MKGMAP) --gmapsupp $(REGIONPATH)/gmapsupps/g{basemap,addr,fixme,osb,boundary,damage,keepright}/gmapsupp.img
	cp $(REGIONPATH)/gmapsupps/gmapsupp/gmapsupp.img $(WEBREGIONDIR)/gmapsupp/gmapsupp.img
else
	cd $(REGIONPATH)/gmapsupps/gmapsupp ; $(MKGMAP) --gmapsupp $(REGIONPATH)/gmapsupps/g{basemap,addr,fixme,osb,boundary,maxspeed}/gmapsupp.img
endif
endif
	mkdir -p $(WEBREGIONDIR)/gmapsupp && \
	cd $(REGIONPATH)/gmapsupps/gmapsupp ; $(COMPR) $(REGIONPATH)/release/gmapsupp_$(REGION).$(DATE).img.$(COMPRESS_SUFFIX) gmapsupp.img && \
	$(call move_and_symlink,gmapsupp,gmapsupp_$(REGION),img.$(COMPRESS_SUFFIX))
#ifeq ($(REGION),germany)
#	ncftpput -b -f ~/.ncftp/juergen gmapsupps/germany $(WEBREGIONDIR)/*.$(COMPRESS_SUFFIX)
#endif
	echo '--------------------------------------- Ende $(REGION)' >> $(PRINTFILE);date >> $(PRINTFILE);echo "-----------------------------------" >> $(PRINTFILE)

$(REGIONPATH)/gmapsupps/gvelomap/gmapsupp.img : $(DEPENDENCY_VELOMAP)
	rm $(REGIONPATH)/gvelomap/* ; \
	cp $(STYLEPATH)/velomap_style/*.txt $(REGIONPATH)/gvelomap/
	$(call handle_layer,velomap,$(VELOMAPOPTIONS),VeloMap_$(REGION),$(VELOMAP_FID),$(VELOMAP_PID),8,$(TILEPATH)/template.args,_,--index --route)

$(REGIONPATH)/gmapsupps/gbasemap/gmapsupp.img : $(DEPENDENCY_BASEMAP)
	$(call make_layer,basemap,$(GBASEMAPOPTIONS),OSM_AiO_basemap_$(REGION),$(BASEMAP_FID),$(BASEMAP_PID),10,$(TILEPATH)/template.args,_,--index --route)

$(REGIONPATH)/gmapsupps/gaddr/gmapsupp.img : $(DEPENDENCY_ADDR)
	$(call make_layer,addr,$(NOBASEMAPOPTIONS) --add-pois-to-areas,OSM_AiO_addr_$(REGION),$(ADDR_FID),$(ADDR_PID),20,$(REGIONPATH)/raw_data/addrsplit/template.args,_addr_)

$(REGIONPATH)/gmapsupps/gfixme/gmapsupp.img : $(DEPENDENCY_FIXME)
	$(call make_layer,fixme,$(NOBASEMAPOPTIONS),OSM_AiO_fixme_$(REGION),$(FIXME_FID),$(FIXME_PID),22,$(TILEPATH)/template.args,_)

$(REGIONPATH)/gmapsupps/gmaxspeed/gmapsupp.img : $(DEPENDENCY_MAXSPEED)
	$(call make_layer,maxspeed,$(NOBASEMAPOPTIONS),OSM_AiO_maxspeed_$(REGION),$(MAXSPEED_FID),$(MAXSPEED_PID),19,$(REGIONPATH)/raw_data/maxspeedsplit/template.args,_maxspeed_)

$(REGIONPATH)/gmapsupps/gboundary/gmapsupp.img : $(DEPENDENCY_BOUNDARY)
	$(call make_layer,boundary,$(NOBASEMAPOPTIONS),OSM_AiO_boundary_$(REGION),$(BOUNDARY_FID),$(BOUNDARY_PID),21,$(REGIONPATH)/raw_data/boundssplit/template.args,_boundary_)

$(REGIONPATH)/gmapsupps/gosb/gmapsupp.img : $(AIOPATH)/openstreetbugs/osbdump_latest.sql.bz2
	bzcat $(AIOPATH)/openstreetbugs/osbdump_latest.sql.bz2 | $(AIOPATH)/osbsql2osm | $(OSMOSIS) --rx - --bb $(BBOX) --nkv keyValueList="type.0" --wx $(REGIONPATH)/raw_data/osb_$(REGION).osm
	rm -f $(REGIONPATH)/gosb/*
	$(call do_stuff,osb,$(NOBASEMAPOPTIONS) --mapname=$(OSB_FID)$(OSB_PID)$(TILE_START) --description='Openstreetbugs' --draw-priority=23,../raw_data/osb_$(REGION).osm,$(OSB_FID),$(OSB_PID),OSM_AiO_osb_$(REGION))

$(REGIONPATH)/gmapsupps/gkeepright/gmapsupp.img : $(DEPENDENCY_KEEPRIGHT)
	$(call make_layer,keepright,$(NOBASEMAPOPTIONS),OSM_AiO_keepright_$(REGION),$(KEEPRIGHT_FID),$(KEEPRIGHT_PID),24,$(REGIONPATH)/raw_data/keeprightsplit/template.args,_keepright_)


$(REGIONPATH)/gmapsupps/gdamage/gmapsupp.img : $(DEPENDENCY_DAMAGE)
	$(call make_layer,damage,$(NOBASEMAPOPTIONS),OSM_AiO_damage_$(REGION),$(DAMAGE_FID),$(DAMAGE_PID),25,$(TILEPATH)/template.args,_)


# contourline target not in use and really deprecated:
$(REGIONPATH)/gmapsupps/gcontourlines/gmapsupp.img:
	cd $(REGIONPATH)/raw_data/contourlines; phyghtmap -a $(LEFT):$(BOTTOM):$(RIGHT):$(TOP) -j $(USE_CORES)
	cd $(REGIONPATH)/gcontourlines; java $(JAVA_OPT) -jar $(AIOPATH)/mkgmap.jar $(NOBASEMAPOPTIONS) --style-file=$(STYLEPATH)/basemap_style --description="contour lines" --family-id=5 --product-id=25  --series-name="OSM-AllInOne-$(KURZ)-contourlines" --family-name=contourlines --mapname=63248345 --draw-priority=100  $(REGIONPATH)/raw_data/contourlines/*.osm ;\
	makensis osmmap.nsi
	cp $(REGIONPATH)/gcontourlines/OSM-AllInOne-$(KURZ)-contourlines.exe $(WEBREGIONDIR)/
	mv $(REGIONPATH)/gcontourlines/gmapsupp.img $(REGIONPATH)/gmapsupps/gcontourlines/gmapsupp.img
	bzip2 -c $(REGIONPATH)/gmapsupps/gcontourlines/gmapsupp.img > $(WEBREGIONDIR)/contourlines.img.bz2
	tar cjf $(REGIONPATH)/release/gcontourlines.tar.bz2 -C $(REGIONPATH) gcontourlines
	cp -f $(REGIONPATH)/release/gcontourlines.tar.bz2 $(WEBREGIONDIR)/
	md5sum -b $(WEBREGIONDIR)/gcontourlines.tar.bz2 > $(WEBREGIONDIR)/gcontourlines.tar.bz2.md5

$(REGIONPATH)/raw_data/keepright.osm.gz : $(AIOPATH)/keepright/keepright_errors.txt.bz2
	bzcat $(AIOPATH)/keepright/keepright_errors.txt.bz2 | awk -f $(AIOPATH)/keepright2osm.awk | $(OSMOSIS) --rx - --bb $(BBOX) --wx $(REGIONPATH)/raw_data/keepright.osm.gz

$(REGIONPATH)/raw_data/keeprightsplit/template.args : $(REGIONPATH)/raw_data/keepright.osm.gz
	cd $(REGIONPATH)/raw_data/keeprightsplit/ && rm *.osm.gz ; $(SPLITTER) $(SPLITTER_OPTIONS_KEEPRIGHT) $(REGIONPATH)/raw_data/keepright.osm.gz 2> $(LOGPATH)/splitter_keepright.log
	$(call process_splitter_files,$(REGIONPATH)/raw_data/keeprightsplit,_keepright_)

$(REGIONPATH)/raw_data/boundssplit/template.args : $(TILEPATH)/template.args
	cd $(REGIONPATH)/raw_data/boundssplit/ && rm *.osm.gz ; $(SPLITTER) $(SPLITTER_OPTIONS_BOUNDARY) $(REGIONPATH)/raw_data/bounds.osm.gz 2> $(LOGPATH)/splitter_boundary.log
	$(call process_splitter_files,$(REGIONPATH)/raw_data/boundssplit,_boundary_)

$(REGIONPATH)/raw_data/maxspeedsplit/template.args : $(TILEPATH)/template.args
	cd $(REGIONPATH)/raw_data/maxspeedsplit/ && rm *.osm.gz ; $(SPLITTER) $(SPLITTER_OPTIONS_MAXSPEED) $(REGIONPATH)/raw_data/maxspeed.osm.gz 2> $(LOGPATH)/splitter_maxspeed.log
	$(call process_splitter_files,$(REGIONPATH)/raw_data/maxspeedsplit,_maxspeed_)

$(REGIONPATH)/raw_data/addrsplit/template.args : $(TILEPATH)/template.args
	cd $(REGIONPATH)/raw_data/addrsplit/ && rm *.osm.gz ; $(SPLITTER) $(SPLITTER_OPTIONS_ADDR) $(REGIONPATH)/raw_data/addr.osm.gz 2> $(LOGPATH)/splitter_addr.log
	$(call process_splitter_files,$(REGIONPATH)/raw_data/addrsplit,_addr_)

# Lets split the file and filter some stuff with osmosis to split it later.
$(TILEPATH)/template.args : $(DATAPATH)
	echo "$(REGION) Auspacken Start:" >> $(PRINTFILE);date >> $(PRINTFILE)
	mkdir -p $(LOGPATH)
# now let the magic begin, clean the TILEPATH and bzcat the .osm.bz2 file to a pipe with 2 Readers (produced by tee)
# The first reader is osmosis that filters boundary,maxspeed and addresses to separat files.
# The second one is the splitter that splits the full .osm file directly for use with basemap and velomap.
	cd $(TILEPATH)/ && rm *.osm.gz ; /usr/bin/time -o $(LOGPATH)/time_splitter bzcat $(DATAPATH) | tee \
	>($(OSMOSIS) --rx - --t \
	--wk keyList="boundary,maxspeed,addr:housenumber,addr:interpolation" --un --t 3 \
	--wk keyList="boundary" --un --wx $(REGIONPATH)/raw_data/bounds.osm.gz \
	--wk keyList="maxspeed" --un --wx $(REGIONPATH)/raw_data/maxspeed.osm.gz \
	--wk keyList="addr:housenumber,addr:interpolation" --un --s --wx $(REGIONPATH)/raw_data/addr_lines.osm.gz \
	--nk keyList="addr:housenumber" --s --wx $(REGIONPATH)/raw_data/addr_points.osm.gz \
	) | $(SPLITTER) $(SPLITTER_OPTIONS) /dev/stdin 2> $(LOGPATH)/splitter.log
	$(OSMOSIS) --rx $(REGIONPATH)/raw_data/addr_lines.osm.gz --rx $(REGIONPATH)/raw_data/addr_points.osm.gz --m --wx $(REGIONPATH)/raw_data/addr.osm.gz
# The following method omit writing temporary files like addr_lines.osm.gz and addr_points.osm.gz to disk by using big caches. The size of the cache is the maximum number of objects and fits exacly into an integer value. The problem is, that it leads to heap space errors... :(
#	cd $(TILEPATH)/ && rm *.osm.gz ; /usr/bin/time -o $(LOGPATH)/time_splitter bzcat $(DATAPATH) | tee \
#	>($(OSMOSIS) --rx - --t outPipe.1=points \
#	--wk keyList="boundary,maxspeed,addr:housenumber,addr:interpolation" --un --t 3 \
#	--wk keyList="boundary" --un --wx $(REGIONPATH)/raw_data/bounds.osm.gz \
#	--wk keyList="maxspeed" --un --wx $(REGIONPATH)/raw_data/maxspeed.osm.gz \
#	--wk keyList="addr:housenumber,addr:interpolation" --un --s --b 2147483647 \
#	--nk inPipe.0=points keyList="addr:housenumber" --s --b 2147483647 \
#	--m --wx $(REGIONPATH)/raw_data/addr.osm.gz ) \
#	| $(SPLITTER) $(SPLITTER_OPTIONS) /dev/stdin 2> $(LOGPATH)/splitter.log
	echo "USE_OLD_AREAS_LIST=$(USE_OLD_AREAS_LIST)" >> $(LOGPATH)/time_splitter
	$(call process_splitter_files,$(TILEPATH),_)
