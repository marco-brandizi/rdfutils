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


msg 'Cleaning-up'
isqlw "SPARQL CLEAR GRAPH <$graph>;"
isqlw "delete from db.dba.load_list where ll_file like '$src_dir%';"
isqlw "ld_dir('$src_dir', '*', '$graph');"
for i in seq $VIRTUOSO_JOBS
do
  isqlw "rdf_loader_run();" &
done

msg "$VIRTUOSO_JOBS loaders started, please wait"
wait $(jobs -p)

msg 'The End'
