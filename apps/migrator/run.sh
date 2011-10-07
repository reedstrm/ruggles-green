#!/bin/bash

# A temp shell script to run the migrator

# TODO(tal): allow to override with command line flags
DATA_ROOT_DIR="/aux/cnx/data_001"

#REPOSITORY_ATOMPUB_URL="http://localhost:8888/atompub"
REPOSITORY_ATOMPUB_URL="http://qa-cnx-repo.appspot.com/atompub"

cd ~/cnx/apps/migrator

classpath=build/classes
for jar in build/jars/*.jar
do
  classpath=${classpath}:${jar}
done

time java  \
  -Xms100m \
  -Xmx4000m \
  -Xss128k \
  -cp ${classpath} \
  org.cnx.migrator.MigratorMain \
  -max_attempts 10 \
  -data_root_dir ${DATA_ROOT_DIR} \
  -repository_atompub_url ${REPOSITORY_ATOMPUB_URL} \
  -shard_filter ".*" \
  -resource_threads 250 \
  -module_threads 250 \
  -collection_threads 250 \
  -migrate_all

