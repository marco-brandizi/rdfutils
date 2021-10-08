#
# Runs the checkpoint command (http://docs.openlinksw.com/virtuoso/checkpoint/) 
# to show information about current no of triples.
#
# if the first argument is true, reports about the current loadings, only the no. of triples in the DB otherwise
#
set -e

is_detail="$1"

cd `dirname $0`
. ./init.sh

message 'Checkpoint'
isql_wrapper "checkpoint;"

if [ "$is_detail" != '' ]; then
  message 'LOAD STATUS INFO'
  isql_wrapper "select ll_state,ll_file,ll_started,ll_error from db.dba.load_list;"
fi

message '3Store Size (#quads)'
isql_wrapper "select count(*) from db.dba. rdf_quad;"
