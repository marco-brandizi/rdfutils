#
#

cd `dirname $0`

. ./init.sh

src_dir="$1"
graph="$2"

if [ "$graph" == '' ]; then

  cat <<EOT
  
  
    $0 <data source dir> <destination named graph>
	
Uploads RDF files into Virtuoso, into the specified named graph.

WARNING: this delete the previous contents of the named graph.

EOT
  
  exit 1	

fi


message 'Cleaning-up'
isql_wrapper "SPARQL CLEAR GRAPH <$graph>;"
isql_wrapper "delete from db.dba.load_list where ll_file like '$src_dir%';"
isql_wrapper "ld_dir('$src_dir', '*', '$graph');"
for i in seq $VIRTUOSO_JOBS
do
  isql_wrapper "rdf_loader_run();" &
done

message "$VIRTUOSO_JOBS loaders started, please wait"
wait $(jobs -p)

message 'The End'
