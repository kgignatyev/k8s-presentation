#!/usr/bin/env bash


echo "starting port forward to Elastic Search:"

podName=`kubectl get pods -l name=es-rc-pod -o=jsonpath='{$.items[0].metadata.name}'`
kubectl port-forward $podName  9200:9200