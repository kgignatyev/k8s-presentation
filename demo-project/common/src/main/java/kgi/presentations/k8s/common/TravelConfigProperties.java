package kgi.presentations.k8s.common;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("travel")
public class TravelConfigProperties {

    @NotBlank
    public String region;

    @NotBlank
    public String access_key_id;

    @NotBlank
    public String secret_access_key;

    @NotBlank
    public String sqs_name;

    @NotBlank
    public String input_bucket;

    @NotBlank
    public String output_bucket;


    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccess_key_id() {
        return access_key_id;
    }

    public void setAccess_key_id(String access_key_id) {
        this.access_key_id = access_key_id;
    }

    public String getSecret_access_key() {
        return secret_access_key;
    }

    public void setSecret_access_key(String secret_access_key) {
        this.secret_access_key = secret_access_key;
    }

    public String getSqs_name() {
        return sqs_name;
    }

    public void setSqs_name(String sqs_name) {
        this.sqs_name = sqs_name;
    }

    public String getOutput_bucket() {
        return output_bucket;
    }

    public void setOutput_bucket(String output_bucket) {
        this.output_bucket = output_bucket;
    }

    public String getInput_bucket() {
        return input_bucket;
    }

    public void setInput_bucket(String input_bucket) {
        this.input_bucket = input_bucket;
    }
}
