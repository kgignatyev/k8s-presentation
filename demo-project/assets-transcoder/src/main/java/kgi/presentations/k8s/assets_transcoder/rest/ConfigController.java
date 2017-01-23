package kgi.presentations.k8s.assets_transcoder.rest;


import kgi.presentations.k8s.common.TravelConfigProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ConfigController {

    @Resource
    TravelConfigProperties configProperties;

    @RequestMapping( path = "/config/info")
    public Map configInfo(){
        Map res = new HashMap();
        res.put("region", configProperties.region);
        res.put("access_key_id", configProperties.access_key_id);
        res.put("sqs_name", configProperties.sqs_name);
        res.put("input_bucket", configProperties.input_bucket);
        res.put("output_bucket", configProperties.output_bucket);
        return res;
    }
}
