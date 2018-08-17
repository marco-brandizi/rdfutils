#!/bin/sh
wdir="$(pwd)"
cd "$(dirname '$0')"
mydir="$(pwd)"
cd "$wdir"

. "$mydir/../init.sh"

while [[ $# -gt 0 ]]
do
  case "$1" in
    --file|--tdb) local_type=$1;;
    *) break;;
  esac

  shift
done

if [ "$#" != 2 ]; then
  cat <<EOT


  `basename "$0"` <sparql tests dir>  <endpoint url base>
  `basename "$0"` ( --file|--tdb ) <sparql tests dir> <data path>

Executes ASK SPARQL queries against a data set and reports if they return true or not. Eventually exits
with an non-zero code if any of the queries failed (ie, it can be used in continous integration systems).

  Notes:

  - Local version requires JENA_HOME, see ../init.sh
  - There are a few environment variables that influences the scripts, again see ../init.sh
  - if a file _prefixes.sparql is found in tests dir that is prepended to all the .sparql files in
  such directory and is expected to have a SPARQL prolog of PREFIX definitions.

EOT
  exit 2
fi

tests_dir="$1"
data_target="$2"

wdir=`pwd`
cd `dirname '$0'`
mydir=`pwd`
cd "$wdir"

printf "\n\n"
ct_ok=0
ct_fail=0
for query_path in $(find "$tests_dir" -name '*.sparql' -not -name '_prefixes.sparql' | sort --ignore-case)
do
  if [ "$local_type" == "" ]; then
    "$mydir"/sparql_ask_endpoint.sh "$query_path" "$data_target"
  else
    "$mydir"/sparql_ask_local.sh "$query_path" "$local_type" "$data_target"
  fi
  result=$?
  if [ $result == 0 ]; then
    ((ct_ok++))
    result_str='OK'
  else
    ((ct_fail++))
    result_str='FAIL'
  fi
  printf "  %s\t%s\n" "$query_path" "$result_str"
done

printf "\n TOTAL TESTS: %d, OK: %d, FAILED: %d\n\n\n" $(($ct_ok + $ct_fail)) $ct_ok $ct_fail
[ $ct_fail == 0 -a $ct_ok -gt 0 ]
