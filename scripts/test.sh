#!/usr/bin/env bash

set -euo pipefail

. "$(git rev-parse --show-toplevel)/scripts/utils.sh"

ensure_mvn

mvn --batch-mode test
