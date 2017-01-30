#!/usr/bin/env groovy

@Grab(group = 'kgi.presentations.k8s', module =  'build-utils', version = '1.0-SNAPSHOT', changing=true)


import static kgi.presentations.k8s.build_utils.BuildUtils.*


def env =( args.length ==0 )?'dev':args[0]


def context = readProperties( '../secrets/travel',env)


processTemplate("src/deployment/es-persistent-volume-aws.tmpl.yml",
        context, "target/k8s/es-persistent-volume-aws."+env+".yml")


copy( "src/deployment/es-persistent-volume-local.yml", "target/k8s/es-persistent-volume-local.yml")
copy( "src/deployment/es-pv-claim.yml", "target/k8s/es-pv-claim.yml")
copy( "src/deployment/es-service.yml", "target/k8s/es-service.yml")