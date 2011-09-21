#!/bin/bash

# A bash script to update QA repo deployment.

# TODO(tal): figure out how to override turning the appcfg prompt off in the standard
# appengine ant macros.

function check_success() {
  exit_status=$?
  if [[ "$exit_status" -ne 0 ]]; then
    echo "$1" >&2
    exit $exit_status
  fi
}


ant clean
check_success "Ant clean failed"

ant war_qa
check_success "Ant war_qa failed"

# TODO(tal): add check that ant returned with OK code

../../third_party/appengine/appengine-java-sdk-1.5.3/bin/appcfg.sh update war
check_success "appcfg.sh update war failed"

