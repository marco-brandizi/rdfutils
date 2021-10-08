#
# Invokes a SPARQL endpoint with the ASK query in the standard input and expects a TRUE result to be
# returned, exits with 0 if that's the case, 1 if not.
#  
# It also spits out a lot of diagnostic output. Do 2>/dev/null if you don't want to see it.
#
set -e

function url_encode {
  echo "$1" | python2.7 -c "import sys, urllib as ul; print ( ul.quote_plus ( sys.stdin.read() ) )"
}

query_path="$1"
endpoint_base="$2"

# Get the query, prepend prefixes.
q_tmp_path="/tmp/sparql_ask_$$_tmp.sparql"
prefixes_path=$(dirname "$query_path")'/_prefixes.sparql'

if [ -e "$prefixes_path" ]; then
  cat "$prefixes_path" >"$q_tmp_path"
fi
cat "$query_path" >>$q_tmp_path
query=$(cat "$q_tmp_path")
rm -f "$q_tmp_path"
echo "$query" >&2 # Some diagnostic
query=$(url_encode "$query")

# Call the endpoint and look for a positive answer
out="$(wget -O - --quiet $WGET_OPTS "$endpoint_base$query")"
echo $out >&2
echo $out | grep -q 'true'
