#!/usr/bin/env bash

set -euo pipefail

. "$(git rev-parse --show-toplevel)/scripts/utils.sh"


ensure_xmlstarlet

echo "repo pom.xml version: $(xmlstarlet sel -t -v '_:project/_:version' pom.xml)"
echo "setting version: ${VERSION}"
xmlstarlet edit \
           --update "_:project/_:version" \
           --value  "${VERSION}" \
           pom.xml \
           > pom.xml.tmp

mv pom.xml.tmp pom.xml
