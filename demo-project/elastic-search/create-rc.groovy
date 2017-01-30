#!/usr/bin/env groovy

@Grab(group = 'kgi.presentations.k8s', module =  'build-utils', version = '1.0-SNAPSHOT', changing=true)


import static kgi.presentations.k8s.build_utils.BuildUtils.*


def image =( args.length ==0 )?'kgignatyev/es:latest':args[0]

processTemplate( "src/deployment/es-rc.tmpl.yml",[image:image], "target/k8s/es-rc.yml")