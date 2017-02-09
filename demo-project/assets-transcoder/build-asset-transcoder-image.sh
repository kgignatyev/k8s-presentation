#!/usr/bin/env bash

TAG=`date +"%Y-%m-%d-%H-%M-%S"`

IMAGE="kgignatyev/assets-transcoder:$TAG"

docker build -t assets-transcoder .

docker tag assets-transcoder:latest $IMAGE


./generate-k8s-assets.groovy $IMAGE

echo "$IMAGE has been created"

