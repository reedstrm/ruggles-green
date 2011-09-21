#!/bin/bash

# A temp shell script to run the migrator

# TODO(tal): allow to override with command line flags
DATA_ROOT_DIR="/aux/cnx/data"

#REPOSITORY_ATOMPUB_URL="http://localhost:8888/atompub"
REPOSITORY_ATOMPUB_URL="http://qa-cnx-repo.appspot.com/atompub"

cd ~/cnx/apps/migrator

cp=build/classes
for jar in build/jars/*.jar
do
  cp=${cp}:${jar}
done

time java  \
  -Xms100m \
  -Xmx1000m \
  -Xss128k \
  -cp ${cp} \
  org.cnx.migrator.MigratorMain \
  -data_root_dir ${DATA_ROOT_DIR} \
  -repository_atompub_url ${REPOSITORY_ATOMPUB_URL} \
  -migrate_modules

