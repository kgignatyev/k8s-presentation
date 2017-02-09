#!/usr/bin/env bash

set -e

kubectl create -f ../infrastructure/monitoring/heapster-rc.yml
kubectl create -f ../infrastructure/monitoring/heapster-service.yml

cd ../elastic-search
./build.sh
cd -


cd ../infrastructure/logging
./process-templates.groovy
./build-image.sh
cd -



kubectl create -f ../elastic-search/target/k8s/es-persistent-volume-local.yml
kubectl create -f ../elastic-search/target/k8s/es-pv-claim.yml
kubectl create -f ../elastic-search/target/k8s/es-rc.yml
kubectl create -f ../elastic-search/target/k8s/es-service.yml


kubectl create -f ../infrastructure/logging/target/k8s/fluentd-secret.dev.yml
kubectl create -f ../infrastructure/logging/target/k8s/fluentd-daemon-set.yml
