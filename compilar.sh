#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
javac --release 11 -encoding UTF-8 -d build src/*.java
jar --create --file gestao-escolar-sprint6.jar --main-class Servidor -C build . web seed.json
