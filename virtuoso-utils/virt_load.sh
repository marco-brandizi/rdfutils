#
#

cd `dirname $0`

if [ "$1" == '-r' ] || [ "$1" == '--recursive' ]; then
  is_recursive='true'
  shift
fi

src_dir="$1"
graph="$2"

if [[ $src_dir =~ ^(-h|--help)$ ]] || [ "$graph" == '' ]; then

  cat <<EOT
  
  
    $0 [-r|--recursive] <data source dir> <destination named graph>
	
Uploads RDF files into Virtuoso, into the specified named graph.

WARNING: this delete the previous contents of the named graph.

EOT
  
  exit 1	

fi

. ./init.sh

ld_cmd='ld_dir'
[[ "$is_recursive" ]] && ld_cmd="${ld_cmd}_all"

message 'Cleaning-up'
isql_wrapper "SPARQL CLEAR GRAPH <$graph>;"
isql_wrapper "delete from db.dba.load_list where ll_file like '$src_dir%';"
isql_wrapper "$ld_cmd('$src_dir', '*', '$graph');"
for i in seq $VIRTUOSO_JOBS
do
  isql_wrapper "rdf_loader_run();" &
done

message "$VIRTUOSO_JOBS loaders started, please wait"
wait $(jobs -p)

message 'The End'
