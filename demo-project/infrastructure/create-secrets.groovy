#!/usr/bin/env groovy
import java.nio.charset.Charset

@Grab(group = 'kgi.presentations.k8s', module =  'build-utils', version = '1.0-SNAPSHOT', changing=true)


import static kgi.presentations.k8s.build_utils.BuildUtils.*


def env =( args.length ==0 )?'dev':args[0]


Properties context = readProperties( '../secrets/travel',env)

StringWriter sw = new StringWriter()

context.store(sw,"Generated for env:"+ env)

String confFileContent64 = Base64.getEncoder().encodeToString(sw.toString().getBytes(Charset.forName("UTF8")))

def out = "target/k8s/travel-secret." + env + ".yml"
processTemplate("src/secret.tmpl.yml",
        [secretName:'travel-properties',
         secretKey:'travel.properties',
         secretContent: confFileContent64], out)

println 'secret is written to file: '+out

