package kgi.presentations.k8s.travelog.vo;

import java.security.PrivateKey;

/**
 * Created by ignak004 on 2/6/17.
 */
public class GCSSecrets {

    public String project_id;
    public String private_key_id;
    public String private_key;
    public String client_email;
    public String client_id;
    public String auth_uri;
    public String token_uri;
    public String auth_provider_x509_cert_url;
    public String client_x509_cert_url;

    public PrivateKey privateKeyInstance;
}
