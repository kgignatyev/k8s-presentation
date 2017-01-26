#!/usr/bin/env groovy
import org.apache.commons.io.FileUtils

@Grab(group = 'kgi.presentations.k8s', module =  'build-utils', version = '1.0-SNAPSHOT', changing=true)


import static kgi.presentations.k8s.build_utils.BuildUtils.*


def image =( args.length ==0 )?'kgignatyev/assets-transcoder:latest':args[0]


def context = [image:image]


processTemplate("src/deployment/assets-transcoder-rc.tmpl.yml",
        context, "target/k8s/assets-transcoder-rc.yml")

copy('src/deployment/assets-transcoder-service.yml','target/k8s/assets-transcoder-service.yml')

