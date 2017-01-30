#!/usr/bin/env bash


echo "starting port forward to remote travelog:"

podName=`kubectl get pods -l app=travelog-ws-pod -o=jsonpath='{$.items[0].metadata.name}'`
kubectl port-forward $podName  9100:9100