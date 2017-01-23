package kgi.presentations.k8s.assets_transcoder;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import kgi.presentations.k8s.common.TravelConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:/etc/travel/travel.properties")
public class TranscoderServiceConfig {


    @Bean
    public TravelConfigProperties appProperties() {
        return new TravelConfigProperties();
    }


    @Bean
    AWSCredentials awsCredentials() {
        TravelConfigProperties props = appProperties();
        return new BasicAWSCredentials(props.access_key_id, props.secret_access_key);
    }


    @Bean
    public AmazonS3Client s3ServiceClient() {
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials());
        s3Client.setRegion(RegionUtils.getRegion(appProperties().region));
        return s3Client;
    }
}
