#!/usr/bin/env bash

set -e

mvn install


TAG=`date +"%Y-%m-%d-%H-%M-%S"`

IMAGE="kgignatyev/travelog-ws:$TAG"

docker build -t travelog-ws .

docker tag travelog-ws:latest $IMAGE


./generate-k8s-assets.groovy $IMAGE

