#!/usr/bin/env bash

set -xeuo pipefail

. "$(git rev-parse --show-toplevel)/scripts/utils.sh"

ensure_tool jdk openjdk-17-jdk
ensure_mvn

mvn --batch-mode compile
