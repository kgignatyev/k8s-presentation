package kgi.presentations.k8s.travelog.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import kgi.presentations.k8s.common.TravelConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/assets/")
public class AssetsApi {

    Logger logger = LoggerFactory.getLogger(this.getClass());



    @Resource
    TravelConfigProperties travelConfigProperties;



    @Resource
    Storage storage;



    @RequestMapping(path = "/check-present",method = RequestMethod.POST)
    public ResponseEntity checkAssetPresent(@RequestBody JsonNode r) throws MalformedURLException {
        String assetUrl = r.get("url").asText();
        URL url = new URL(assetUrl);
        String assetPath = url.getPath().substring(1);//removing first slash
        int firstSlash = assetPath.indexOf('/');
        String bucket = assetPath.substring(0,firstSlash);
        String key = assetPath.substring(firstSlash+1);
        try {
            Blob obj = storage.get(bucket, key);
            return ResponseEntity.ok(obj.getMediaLink());
        }catch (Exception e){
            logger.error("storage problems", e);
            return ResponseEntity.notFound().build();
        }



    }

    String presignPutURIPrefix = "/api/assets/generate-post-url/";
    int presignPutURIPrefixLength = presignPutURIPrefix.length();

    @RequestMapping(path = "/generate-post-url/**",
            method = RequestMethod.POST,produces = "application/json")
    public Map presignPutRequest(@RequestBody JsonNode b, HttpServletRequest request) {
        String assetName = request.getRequestURI().substring(presignPutURIPrefixLength);
        String bucketName = travelConfigProperties.getInput_bucket();
        String contentType = b.get("content-type").asText();
        String travelogId = b.get("travelogId").asText();
        String objectKey = travelogId +"/" + assetName;

        logger.debug("Pre-signing for bucket={} and key={} content-type={}",bucketName, objectKey, contentType);


        Map res = new HashMap();
        BlobInfo blob = Blob.newBuilder( bucketName, objectKey).setContentType(contentType).build();
        res.put("url", storage.signUrl( blob,10, TimeUnit.MINUTES,
                Storage.SignUrlOption.withContentType(),
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT) ).toString());
        return res;
    }

    private Date putRequestExpiration() {
        return new Date(System.currentTimeMillis()+ 180000);
    }



}
