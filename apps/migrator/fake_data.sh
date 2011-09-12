#!/bin/bash

# A script to generate fake CNX  data for testing the migrator.

# TODO(tal): generate also modules and collections

# Root directory for generated data
ROOT="/usr/local/cnx/data"

# Number of fake resources to generate
RESOURCE_COUNT=500

# Create resources root directory
RESOURCES_ROOT="${ROOT}/resources"
mkdir -p ${RESOURCES_ROOT}

# Create resource shard directories
for ((i = 0; i < 1000; i += 1))
do
  shard_id=`printf "%03d" $i`
  shard_path="${RESOURCES_ROOT}/${shard_id}"
  echo ${shard_path}
  mkdir "${shard_path}"
done

# Create resource items
for ((i = 0; i < ${RESOURCE_COUNT}; i += 1))
do
  echo
  shard_num=`expr $i % 1000`
  shard_id=`printf "%03d" ${shard_num}`
  resource_id=`printf "%06d" $i`
  echo "resource id = ${resource_id}"
  item_path=${RESOURCES_ROOT}/${shard_id}/${resource_id}
  echo "  location = ${item_path}"
  mkdir -p ${item_path}

  # Create resource file
  echo "  * Generating resource file:"
  #unit_size="1k"
  unit_size="1M"
  unit_count="$[ ( $RANDOM % 10 )  + 1 ]"
  echo "    resource size = ${unit_count} x ${unit_size}"
  resource_file="${item_path}/resource_data"
  dd if=/dev/urandom of=${resource_file} bs=${unit_size} count=${unit_count} 2>&1 | sed 's/^/    /'

  # Create property file
  echo
  echo "  * Generating properties file:"
  resource_size=$(stat -c%s ${resource_file})
  resource_md5=$(md5sum ${resource_file} | cut -f1 -d' ')
  property_file="${item_path}/resource_properties.txt"
  echo "# Resource propreties (Java property file format)" > "${property_file}"
  echo "file_name = fake.bin" >> "${property_file}"
  echo "content_type = application/fake" >> "${property_file}"
  echo "data_size = ${resource_size}" >> "${property_file}"
  echo "data_md5 = ${resource_md5}" >> "${property_file}"
  sed 's/^/    /' ${property_file}
done


