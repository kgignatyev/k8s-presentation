#!/usr/bin/env groovy

@Grab(group = 'kgi.presentations.k8s', module =  'build-utils', version = '1.0-SNAPSHOT', changing=true)


import static kgi.presentations.k8s.build_utils.BuildUtils.*


def image =( args.length ==0 )?'kgignatyev/assets-transcoder:lates':args[0]


def context = [image:image]


processTemplate("src/deployment/assets-transcoder-rc.tmpl.yml",
        context, "target/k8s/assets-transcoder-rc.yml")
