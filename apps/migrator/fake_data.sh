#!/bin/bash

# A script to generate fake CNX  data for testing the migrator.

# TODO(tal): generate module and collection versions.

# Root directory for generated data
# TODO(tal): allow to override with command line flags
ROOT="/aux/cnx/data"

# Number of fake items to generate
# NOTE(tal): can have at most 99999 since we currently reserve repo ids [1-99999]
RESOURCE_COUNT=99999

MODULE_COUNT=20000
VERSIONS_PER_MODULE=5

COLLECTIONS=1000
VERSIONS_PER_COLLECTION=10

# Root directories by entity types
RESOURCES_ROOT="${ROOT}/resources"
MODULES_ROOT="${ROOT}/modules"
COLLECTIONS_ROOT="${ROOT}/collections"

# Create shard directories under given directory
#
# $1 = parent directory of shards
function create_shards {
  local parent=$1
  for ((i = 0; i < 1000; i += 1))
  do
    local shard_id=`printf "%03d" $i`
    local shard_path="${parent}/${shard_id}"
    echo "Creating shard directory: ${shard_path}"
    mkdir "${shard_path}"
  done
}


# Create fake resources
function create_fake_resources {
  mkdir -p ${RESOURCES_ROOT}
  create_shards ${RESOURCES_ROOT}

  for ((i = 1; i <= ${RESOURCE_COUNT}; i += 1))
  do
    echo
    local shard_num=`expr $i % 1000`
    local shard_id=`printf "%03d" ${shard_num}`
    local item_id=`printf "%06d" $i`
    local item_path=${RESOURCES_ROOT}/${shard_id}/${item_id}
    echo "  location = ${item_path}"
    mkdir -p ${item_path}

    # Create resource file
    echo "  * Generating resource file:"
    local unit_size="1k"
    #local unit_size="1M"
    local unit_count="$[ ( $RANDOM % 400 )  + 1 ]"
    echo "    resource size = ${unit_count} x ${unit_size}"
    local resource_file="${item_path}/resource"
    dd if=/dev/urandom of=${resource_file} bs=${unit_size} count=${unit_count} 2>&1 | sed 's/^/    /'

    # Create property file
    echo
    echo "  * Generating properties file:"
    local resource_size=$(stat -c%s ${resource_file})
    local resource_md5=$(md5sum ${resource_file} | cut -f1 -d' ')
    local property_file="${item_path}/properties"
    echo "# Resource propreties (Java property file format)" > "${property_file}"
    echo "file_name = fake.bin" >> "${property_file}"
    echo "content_type = application/fake" >> "${property_file}"
    echo "data_size = ${resource_size}" >> "${property_file}"
    echo "data_md5 = ${resource_md5}" >> "${property_file}"
    sed 's/^/    /' ${property_file}
  done
}

# Create fake modules
function create_fake_modules {
  mkdir -p ${MODULES_ROOT}
  create_shards ${MODULES_ROOT}

  for ((i = 1; i <= ${MODULE_COUNT}; i += 1))
  do
    echo
    local shard_num=`expr $i % 1000`
    local shard_id=`printf "%03d" ${shard_num}`
    local module_id=`printf "%06d" $i`
    local module_path=${MODULES_ROOT}/${shard_id}/${module_id}
    echo "  module = ${module_path}"
    mkdir -p ${module_path}

    for ((v = 1; v <= ${VERSIONS_PER_MODULE} ; v += 1))
    do
      local version_id=`printf "%04d" $v`
      local version_path=${module_path}/${version_id}
      echo "  module version = ${version_path}"
      mkdir -p ${version_path}

      # Create CNXML file
      echo "  * Generating cnxml file:"
      local unit_size="1k"
      local unit_count="$[ ( $RANDOM % 100 )  + 1 ]"
      echo "    module size = ${unit_count} x ${unit_size}"
      local cnxml_file="${version_path}/cnxml.xml"
      dd if=/dev/urandom of=${cnxml_file} bs=${unit_size} count=${unit_count} 2>&1 | sed 's/^/    /'

      # Create property file with module version info
      echo "  * Generating module version property file:"
      local version_properties="${version_path}/properties.txt"
      echo "# Module version properties Java properties format" > ${version_properties}
      echo "# Actual properties TBD" > ${version_properties}
      echo "original_version = 12.34" >> ${version_properties}
      echo "author = bla bla bla" >> ${version_properties}
      echo "publish_time = [TBD]" >> ${version_properties}
      sed 's/^/    /' ${version_properties}

      # Create property file with resource mapping
      echo "  * Generating resource map file:"
      local version_map="${version_path}/resources.txt"
      echo "# Resource map in Java properties format" > ${version_map}
      echo "# Content types are inherited from the resources" >> ${version_map}
      echo "abc.png = 1234" >> ${version_map}
      echo "resource_2a.pdf = 456" >> ${version_map}
      sed 's/^/    /' ${version_map}

      # Create module version abstract file
      echo "  * Generating abstract file:"
      local abstract="${version_path}/abstract.txt"
      echo "Lorem ipsum dolor sit amet, enim pharetra, nec" > ${abstract}
      echo "voluptas integer scelerisque, dictum tristique" >> ${abstract}
      echo "vestibulum luctus, vestibulum consectetur. Euismod"  >> ${abstract}
      echo "nonummy cras, per per. Vehicula cras mollis," >> ${abstract}
      echo "ac wisi quisque aenean lacus elit." >> ${abstract}
      sed 's/^/    /' ${abstract}

    done
  done
}

# Create fake collections
function create_fake_collections {
  mkdir -p ${COLLECTIONS_ROOT}
  create_shards ${COLLECTIONS_ROOT}

  for ((i = 1; i <= ${COLLECTIONS}; i += 1))
  do
    echo
    local shard_num=`expr $i % 1000`
    local shard_id=`printf "%03d" ${shard_num}`
    local collection_id=`printf "%06d" $i`
    local collection_path=${COLLECTIONS_ROOT}/${shard_id}/${collection_id}
    echo "  location = ${collection_path}"
    mkdir -p ${collection_path}

    for ((v = 1; v <= ${VERSIONS_PER_COLLECTION} ; v += 1))
    do
      local version_id=`printf "%04d" $v`
      local version_path=${collection_path}/${version_id}
      echo "  collection version = ${version_path}"
      mkdir -p ${version_path}

      # Create collection version file
      echo "  * Generating collection version file:"
      local unit_size="1k"
      local unit_count="$[ ( $RANDOM % 20 )  + 1 ]"
      echo "    collection size = ${unit_count} x ${unit_size}"
      local collection_file="${version_path}/colxml.xml"
      dd if=/dev/urandom of=${collection_file} bs=${unit_size} count=${unit_count} 2>&1 | sed 's/^/    /'
    done
  done
}

function main {
  create_fake_resources
  create_fake_modules
  create_fake_collections
}

main

