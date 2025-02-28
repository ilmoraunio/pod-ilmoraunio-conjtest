#!/bin/bash

set -x

DIRNAME="$(dirname $0)"

cd "$DIRNAME/.."

bb uberjar target/pod-ilmoraunio-conjtest.jar -m pod-ilmoraunio-conjtest.core

[ ! -f bb ] && curl -sLO https://github.com/babashka/babashka/releases/download/v1.3.191/babashka-1.3.191-macos-aarch64.tar.gz && tar xzvf babashka-1.3.191-macos-aarch64.tar.gz

cat bb target/pod-ilmoraunio-conjtest.jar > pod-ilmoraunio-conjtest

chmod +x pod-ilmoraunio-conjtest
