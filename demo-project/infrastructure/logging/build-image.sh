#!/usr/bin/env bash

./process-templates.groovy

TAG=`date +"%Y-%m-%d-%H-%M-%S"`

docker build -t fluentd .

image="kgignatyev/fluentd:$TAG"

docker tag fluentd:latest $image


docker push $image

echo "Created image $image"

./create-k8s-artifacts.groovy $image