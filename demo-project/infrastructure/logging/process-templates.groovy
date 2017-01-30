#!/usr/bin/env groovy

@Grab(group = 'kgi.presentations.k8s', module =  'build-utils', version = '1.0-SNAPSHOT', changing=true)

import static kgi.presentations.k8s.build_utils.BuildUtils.*
import static org.apache.commons.io.FileUtils.*


def env =( args.length ==0 )?'dev':args[0]

Properties context = readProperties( '../../secrets/travel',env)

processTemplate("src/fluentd.tmpl.conf",
        context, 'target/k8s/fluentd.conf')

//now lets create mountable secret

String confFileContent64 = Base64.getEncoder().encodeToString(readFileToByteArray( new File("target/k8s/fluentd.conf") ))

def out = "target/k8s/fluentd-secret." + env + ".yml"

processTemplate("../src/secret.tmpl.yml",
        [secretName:'fluentd-conf',
         secretKey:'fluentd.conf',
         secretContent: confFileContent64], out)

