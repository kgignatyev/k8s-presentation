#!/usr/bin/env bash

set -e

mvn install

./build-asset-transcoder-image.sh