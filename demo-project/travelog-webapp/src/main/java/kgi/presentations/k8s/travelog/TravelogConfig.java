package kgi.presentations.k8s.travelog;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.storage.StorageScopes;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import kgi.presentations.k8s.common.TravelConfigProperties;
import kgi.presentations.k8s.travelog.svc.TravelogService;
import kgi.presentations.k8s.travelog.vo.GCSSecrets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.Resource;
import java.io.File;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.Set;

@Configuration
@PropertySource("file:/etc/travelog/travel-gcs.properties")
public class TravelogConfig {

    private FileDataStoreFactory dataStoreFactory;

    @Bean
    TravelConfigProperties configProperties(){
        return new TravelConfigProperties();
    }


    @Resource
    ObjectMapper om;


    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/storage_sample");


    @Bean
    public GCSSecrets gcsSecrets() throws Exception {
        GCSSecrets gcsSecrets = om.readValue(new File("/etc/travelog/travelog-gcs-sercrets.json"), GCSSecrets.class);

        byte[] bytes = PemReader.readFirstSectionAndClose(new StringReader(gcsSecrets.private_key), "PRIVATE KEY")
                .getBase64DecodedBytes();
        PrivateKey serviceAccountPrivateKey =
                SecurityUtils.getRsaKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
        gcsSecrets.privateKeyInstance = serviceAccountPrivateKey;
        return gcsSecrets;
    }

    @Bean
    public Storage gceStorage() throws Exception {

        GCSSecrets s = gcsSecrets();
        Set<String> scopes = Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL);

        ServiceAccountCredentials saCredentials = new ServiceAccountCredentials(s.client_id,s.client_email, s.privateKeyInstance,s.private_key_id,scopes);

        StorageOptions storageOptions = StorageOptions.newBuilder().setCredentials(saCredentials).setProjectId(s.project_id).build();

        return storageOptions.getService();
    }





    @Bean
    TravelogService travelogService(){
        return new TravelogService();
    }

}
