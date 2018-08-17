#
# Common definitions for Virtuoso scripts. This file is expected to be run via the 'source' command. 
#

: ${VIRTUOSO_USER:=dba}
: ${VIRTUOSO_PASSWORD:=dba}
: ${VIRTUOSO_BIN_DIR:=/usr/local/bin

# Number of parallel jobs that can be sent to Virtuoso commands. Used with rdf_loader_run() and similar cases
#
: ${VIRTUOSO_JOBS:=10}

export VIRTUOSO_BIN_DIR VIRTUOSO_USER VIRT_PASSWORD

# This is used in scripts that invoke SPARQL endpoints
export WGET_OPTS='--no-check-certificate --user='foo' --password='changeme''

# Jena is used by certain scripts, such as sparql_unit_testing/
#
if [ "$JENA_HOME" == "" ]
then
  echo -e "\tWARNING: no JENA_HOME defined"
fi

function isql_wrapper
{
  $VIRTUOSO_BIN_DIR/isql 1111 $VIRTUOSO_USER $VIRTUOSO_PASSWORD exec="$@"
}

function message
{
  echo -e "\n\n\t\t--------- $@ ---------\n"
}
