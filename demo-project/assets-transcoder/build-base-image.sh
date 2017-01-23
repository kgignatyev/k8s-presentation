#!/usr/bin/env bash

TAG=`date +"%Y-%m-%d-%H-%M-%S"`

IMAGE="kgignatyev/assets-transcoder-base:$TAG"

docker build -t assets-transcoder-base base

docker tag assets-transcoder:latest $IMAGE

