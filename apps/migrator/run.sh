#!/bin/bash

# A temp shell script to run the migrator

DATA_ROOT_DIR="/usr/local/cnx/data"
#REPOSITORY_ATOMPUB_URL="http://localhost:8888/atompub"
REPOSITORY_ATOMPUB_URL="http://qa-cnx-repo.appspot.com/atompub"
WORKER_THREADS=100

cd ~/cnx/apps/migrator

cp=build/classes
for jar in build/jars/*.jar
do
  cp=${cp}:${jar}
done

java -cp ${cp}  org.cnx.migrator.MigratorMain \
  -data_root_dir ${DATA_ROOT_DIR} \
  -repository_atompub_url ${REPOSITORY_ATOMPUB_URL} \
  -worker_threads ${WORKER_THREADS} 

