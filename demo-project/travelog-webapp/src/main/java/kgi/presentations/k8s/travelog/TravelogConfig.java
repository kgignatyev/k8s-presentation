package kgi.presentations.k8s.travelog;

import com.fasterxml.jackson.databind.ObjectMapper;
import kgi.presentations.k8s.common.TravelConfigProperties;
import kgi.presentations.k8s.travelog.svc.S3UploadsSignatureHandler;
import kgi.presentations.k8s.travelog.svc.TravelogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Resource;

@Configuration
@PropertySource("file:/etc/travelog/travel.properties")
public class TravelogConfig {

    @Resource
    ObjectMapper om;

    @Bean
    TravelConfigProperties configProperties(){
        return new TravelConfigProperties();
    }


    @Bean
    TravelogService travelogService(){
        return new TravelogService();
    }

    @Bean
    S3UploadsSignatureHandler s3UploadsSignatureHandler(){
        TravelConfigProperties p = configProperties();
        return  new S3UploadsSignatureHandler( p.access_key_id, p.secret_access_key, om);
    }
}
