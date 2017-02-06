#!/usr/bin/env bash


curl -H "Content-Type: application/json" -X PUT --data @src/test/resources/sample-post-1.json http://localhost:9200/travelog/posts/1
curl -H "Content-Type: application/json" -X PUT --data @src/test/resources/sample-post-2.json http://localhost:9200/travelog/posts/2