To run the project
---

* Install minikube
https://github.com/kubernetes/minikube

* Create account with logz.io
http://logz.io/


* Create AWS account
 >* create S3 bucket for uploading assets, 
 and set CORS configuration to allow any (or localhost:7100) 
 to perform PUT operation 
  
 >* create S3 bucket for serving assets, 
 and make it available for read only access to anybody
 
* Edit secrets/travelog.properties file and set values for 
your environment. 
   




