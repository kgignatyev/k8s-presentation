SQL should have publishing enabled for source
---

sample policy:
```
  {
   "Sid": "kgi1",
   "Effect": "Allow",
   "Principal": {
     "AWS": "*"  
   },
   "Action": [
    "SQS:SendMessage"
   ],
   "Resource": "SQS-ARN",
   "Condition": {
      "ArnLike": {          
      "aws:SourceArn": "arn:aws:s3:*:*:k8s-presentation-in"    
    }
   }
  }  
```  

