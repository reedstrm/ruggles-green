#!/bin/bash

# A script to generate fake CNX exports for a given set of
# modules and collections in CNX migration data export format.

# Root directory for generated data
# TODO(tal): allow to override with command line flags
ROOT="/aux/cnx/data_001"

# Number of fake items to generate
# NOTE(tal): can have at most 99999 since we currently reserve repo ids [1-99999]
#RESOURCE_COUNT=99999

#MODULE_COUNT=20000
#VERSIONS_PER_MODULE=5

#COLLECTIONS=1000
#VERSIONS_PER_COLLECTION=10

# Root directories by entity types
#RESOURCES_ROOT="${ROOT}/resources"
MODULES_ROOT="${ROOT}/modules"
COLLECTIONS_ROOT="${ROOT}/collections"

function create_fake_export {
  local export_path="$1"
  local unit_size="$2"
  local max_units="$3"
  echo "  * Generating export ${export_path} :"
  #local unit_size="1k"
  #local unit_size="1M"
  local unit_count="$[ ( $RANDOM % ${max_units}) + 1 ]"
  echo "    resource size = ${unit_count} x ${unit_size}"
  dd if=/dev/urandom of=${export_path} bs=${unit_size} count=${unit_count} 2>&1 | sed 's/^/    /'
}

function create_entity_fake_exports {
  local entity_dir="$1"
  local exports_dir="${entity_dir}/exports"
  mkdir -p ${exports_dir}
  create_fake_export ${exports_dir}/std_pdf.pdf   1k  1000
  create_fake_export ${exports_dir}/std_epub.epub 1k  1000
  create_fake_export ${exports_dir}/std_zip.zip   1k  1000
}

# Create fake resources for all modules and versions
function create_modules_fake_exports {
  local shards=`ls -d ${MODULES_ROOT}/00*`
  for shard in ${shards}; do
    echo "shard: ${shard}"
    local module=`ls -d ${shard}/*`
    for module in ${module}; do
      echo "  module: ${module}"
      local versions=`ls -d ${module}/*`
      for version in ${versions}; do
        echo "    version: ${version}"
        create_entity_fake_exports ${version}
      done
    done
  done
}

function main {
  cd ${ROOT}
  create_modules_fake_exports
}

main

