#!/usr/bin/env bash

set -e

TAG=`date +"%Y-%m-%d-%H-%M-%S"`

IMAGE="kgignatyev/es:$TAG"

docker build -t es .

docker tag es:latest $IMAGE


./create-rc.groovy $IMAGE

