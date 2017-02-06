package kgi.presentations.k8s.travelog;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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
    AWSCredentials awsCredentials() {
        TravelConfigProperties props = configProperties();
        return new BasicAWSCredentials(props.access_key_id, props.secret_access_key);
    }


    @Bean
    public AmazonS3 s3ServiceClient() {
//        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials());
//        s3Client.setRegion(RegionUtils.getRegion(configProperties().region));
//        return s3Client;
        return AmazonS3ClientBuilder.standard()
                .withRegion(configProperties().region)
                .withCredentials( new AWSStaticCredentialsProvider( awsCredentials() )).build();
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
