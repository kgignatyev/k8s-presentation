#!/usr/bin/env groovy


@Grab(group = 'kgi.presentations.k8s', module =  'build-utils', version = '1.0-SNAPSHOT', changing=true)


import static kgi.presentations.k8s.build_utils.BuildUtils.*


def env =( args.length ==0 )?'dev':args[0]


Properties context = readProperties( '../secrets/travel',env)

def writer =  new FileWriter('/etc/travel/travel.properties')
context.store(writer,'Generated for environment:'+ env)
writer.flush()
