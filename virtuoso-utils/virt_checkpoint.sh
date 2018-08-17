#
# Runs the checkpoint command (http://docs.openlinksw.com/virtuoso/checkpoint/) 
# to show information about current no of triples.
#
# if the first argument is true, reports about the current loadings, only the no. of triples in the DB otherwise
#

is_detail="$1"

cd `dirname $0`
. ./init.sh

msg 'Checkpoint'
isqlw "checkpoint;"

if [ "$report_flag" != '' ]; then
  msg 'LOAD STATUS INFO'
  isqlw "select ll_state,ll_file,ll_started,ll_error from db.dba.load_list;"
fi

message '3Store Size (#quads)'
isqlw "select count(*) from db.dba. rdf_quad;"
