#
# Works like sparql_ask.sh, but against a data file (using jena.)
#

# Get the query, strip comments away, prepend common prefixes
query_path="$1"
type="$2"
data_path="$3"

q_tmp_path="/tmp/sparql_ask_$$_tmp.sparql"
prefixes_path=$(dirname "$query_path")'/_prefixes.sparql'
if [ -e "$prefixes_path" ]; then
  cat "$prefixes_path" >"$q_tmp_path"
fi
cat "$query_path" >>"$q_tmp_path"
cat "$q_tmp_path" >&2

if [ "$type" == '--file' ]; then
  cmd=sparql
  in_opt=data
else
  cmd=tdbquery
  in_opt=loc
fi
cmd="$JENA_HOME/bin/$cmd"

out=$($cmd --$in_opt="$data_path" --query="$q_tmp_path" --results=TSV)
#x=$(sparql --data='/tmp/all.ttl' --query='$q_tmp_path' --results=TSV)
rm -f "$q_tmp_path"

echo $out >&2
echo $out | grep -q 'true'
